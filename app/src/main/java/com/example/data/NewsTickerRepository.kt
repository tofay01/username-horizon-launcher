package com.example.data

import com.example.BuildConfig
import com.example.model.LauncherLanguage
import com.example.model.NewsArticle
import com.example.model.NewsCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class NewsFeedSource(
    val sourceName: String,
    val url: String,
    val category: NewsCategory,
)

sealed interface NewsFetchResult {
    data class Success(val articles: List<NewsArticle>) : NewsFetchResult
    data class Failure(val errorMessage: String) : NewsFetchResult
}

class NewsTickerRepository(
    private val httpClient: OkHttpClient = defaultClient(),
) {

    suspend fun fetchNews(
        language: LauncherLanguage,
        enabledCategories: Set<NewsCategory>,
    ): NewsFetchResult = withContext(Dispatchers.IO) {
        if (enabledCategories.isEmpty()) {
            return@withContext NewsFetchResult.Success(emptyList())
        }
        val sourcesByCategory = sourcesForLanguage(language)
            .filterKeys { enabledCategories.contains(it) }

        val resultsPerCategory = coroutineScope {
            sourcesByCategory.map { (category, sources) ->
                category to async {
                    fetchCategoryArticles(sources)
                }
            }.map { (category, deferred) ->
                category to runCatching { deferred.await() }.getOrDefault(emptyList())
            }.toMap()
        }

        val interleaved = interleaveAndDeduplicate(resultsPerCategory, maxTotal = 30)
        if (interleaved.isNotEmpty()) {
            NewsFetchResult.Success(interleaved)
        } else {
            NewsFetchResult.Failure("Unable to reach news feeds")
        }
    }

    private fun fetchCategoryArticles(sources: List<NewsFeedSource>): List<NewsArticle> {
        val collected = mutableListOf<NewsArticle>()
        for (source in sources) {
            val xml = downloadFeedXml(source.url) ?: continue
            val parsed = parseRssOrAtomXml(
                xml = xml,
                category = source.category,
                sourceName = source.sourceName,
            )
            if (parsed.isNotEmpty()) {
                collected.addAll(parsed.take(8))
                if (collected.size >= 10) break
            }
        }
        return collected
    }

    private fun downloadFeedXml(url: String): String? = runCatching {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "HorizonTVLauncher/${BuildConfig.VERSION_NAME} (Android TV)")
            .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
            .build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@use null
            resp.body?.string()?.takeIf { it.isNotBlank() }
        }
    }.getOrNull()

    companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val NUMERIC_ENTITY_REGEX = Regex("&#(x?[0-9a-fA-F]+);")

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        fun sourcesForLanguage(language: LauncherLanguage): Map<NewsCategory, List<NewsFeedSource>> =
            when (language) {
                LauncherLanguage.AR -> mapOf(
                    NewsCategory.POLITICS to listOf(
                        NewsFeedSource("BBC عربي", "https://feeds.bbci.co.uk/arabic/middleeast/rss.xml", NewsCategory.POLITICS),
                        NewsFeedSource("فرانس 24", "https://www.france24.com/ar/rss", NewsCategory.POLITICS),
                        NewsFeedSource("DW عربية", "https://rss.dw.com/xml/rss-ar-all", NewsCategory.POLITICS),
                    ),
                    NewsCategory.SPORTS to listOf(
                        NewsFeedSource("سكاي نيوز رياضة", "https://www.skynewsarabia.com/web/rss/sport.xml", NewsCategory.SPORTS),
                        NewsFeedSource("فرانس 24 رياضة", "https://www.france24.com/ar/sport/rss", NewsCategory.SPORTS),
                        NewsFeedSource("BBC Sport", "https://feeds.bbci.co.uk/sport/rss.xml", NewsCategory.SPORTS),
                    ),
                    NewsCategory.WORLD to listOf(
                        NewsFeedSource("BBC العالم", "https://feeds.bbci.co.uk/arabic/worldnews/rss.xml", NewsCategory.WORLD),
                        NewsFeedSource("BBC عربي", "https://feeds.bbci.co.uk/arabic/rss.xml", NewsCategory.WORLD),
                        NewsFeedSource("فرانس 24", "https://www.france24.com/ar/rss", NewsCategory.WORLD),
                    ),
                )

                LauncherLanguage.FR -> mapOf(
                    NewsCategory.POLITICS to listOf(
                        NewsFeedSource("France 24", "https://www.france24.com/fr/france/rss", NewsCategory.POLITICS),
                        NewsFeedSource("Le Monde", "https://www.lemonde.fr/politique/rss_full.xml", NewsCategory.POLITICS),
                        NewsFeedSource("RFI", "https://www.rfi.fr/fr/rss", NewsCategory.POLITICS),
                    ),
                    NewsCategory.SPORTS to listOf(
                        NewsFeedSource("France 24 Sport", "https://www.france24.com/fr/sports/rss", NewsCategory.SPORTS),
                        NewsFeedSource("Le Monde Sport", "https://www.lemonde.fr/sport/rss_full.xml", NewsCategory.SPORTS),
                        NewsFeedSource("Eurosport", "https://www.eurosport.fr/rss.xml", NewsCategory.SPORTS),
                    ),
                    NewsCategory.WORLD to listOf(
                        NewsFeedSource("France 24 Monde", "https://www.france24.com/fr/monde/rss", NewsCategory.WORLD),
                        NewsFeedSource("Le Monde", "https://www.lemonde.fr/international/rss_full.xml", NewsCategory.WORLD),
                        NewsFeedSource("France 24", "https://www.france24.com/fr/rss", NewsCategory.WORLD),
                    ),
                )

                LauncherLanguage.EN -> mapOf(
                    NewsCategory.POLITICS to listOf(
                        NewsFeedSource("BBC Politics", "https://feeds.bbci.co.uk/news/politics/rss.xml", NewsCategory.POLITICS),
                        NewsFeedSource("NPR Politics", "https://feeds.npr.org/1014/rss.xml", NewsCategory.POLITICS),
                        NewsFeedSource("France 24", "https://www.france24.com/en/rss", NewsCategory.POLITICS),
                    ),
                    NewsCategory.SPORTS to listOf(
                        NewsFeedSource("BBC Sport", "https://feeds.bbci.co.uk/sport/rss.xml", NewsCategory.SPORTS),
                        NewsFeedSource("ESPN", "https://www.espn.com/espn/rss/news", NewsCategory.SPORTS),
                        NewsFeedSource("France 24 Sport", "https://www.france24.com/en/sport/rss", NewsCategory.SPORTS),
                    ),
                    NewsCategory.WORLD to listOf(
                        NewsFeedSource("BBC World", "https://feeds.bbci.co.uk/news/world/rss.xml", NewsCategory.WORLD),
                        NewsFeedSource("Al Jazeera", "https://www.aljazeera.com/xml/rss/all.xml", NewsCategory.WORLD),
                        NewsFeedSource("France 24 World", "https://www.france24.com/en/world/rss", NewsCategory.WORLD),
                    ),
                )
            }

        fun parseRssOrAtomXml(
            xml: String,
            category: NewsCategory,
            sourceName: String,
        ): List<NewsArticle> = runCatching {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = false
            }
            val parser = factory.newPullParser().apply {
                setInput(StringReader(xml.trimStart()))
            }
            val items = mutableListOf<NewsArticle>()
            var inItem = false
            var isAtomEntry = false
            var title = ""
            var summary = ""
            var link = ""
            var pubDate = ""
            var currentTag = ""

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name.orEmpty().lowercase()
                        if (tagName == "item" || tagName == "entry") {
                            inItem = true
                            isAtomEntry = tagName == "entry"
                            title = ""
                            summary = ""
                            link = ""
                            pubDate = ""
                        } else if (inItem) {
                            currentTag = tagName
                            if (tagName == "link") {
                                val href = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                                val rel = parser.getAttributeValue(null, "rel")?.trim().orEmpty()
                                if (href.isNotBlank() && (rel.isEmpty() || rel == "alternate" || link.isBlank())) {
                                    link = href
                                }
                            }
                        }
                    }

                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> {
                        if (inItem) {
                            val chunk = parser.text.orEmpty()
                            when (currentTag) {
                                "title" -> title += chunk
                                "description", "summary", "content", "content:encoded" -> {
                                    if (!isAtomEntry || summary.length < 600) {
                                        summary += chunk
                                    }
                                }
                                "link" -> if (link.isBlank()) link += chunk
                                "pubdate", "updated", "published", "dc:date" -> pubDate += chunk
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name.orEmpty().lowercase()
                        if ((tagName == "item" || tagName == "entry") && inItem) {
                            val cleanTitle = cleanFeedText(title)
                            val cleanSummary = cleanFeedText(summary).take(260)
                            val cleanLink = link.trim()
                            if (cleanTitle.isNotBlank()) {
                                val id = "${category.id}:${cleanTitle.lowercase().hashCode()}"
                                items.add(
                                    NewsArticle(
                                        id = id,
                                        title = cleanTitle,
                                        summary = if (cleanSummary == cleanTitle) "" else cleanSummary,
                                        link = cleanLink,
                                        sourceName = sourceName,
                                        category = category,
                                        publishedAt = pubDate.trim().take(25),
                                    )
                                )
                            }
                            inItem = false
                            currentTag = ""
                        } else if (inItem && tagName == currentTag) {
                            currentTag = ""
                        }
                    }
                }
                eventType = parser.next()
            }
            items
        }.getOrDefault(emptyList())

        fun cleanFeedText(raw: String): String {
            if (raw.isBlank()) return ""
            val strippedTags = raw.replace(HTML_TAG_REGEX, " ")
            val namedDecoded = strippedTags
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .replace("&laquo;", "«")
                .replace("&raquo;", "»")
                .replace("&ndash;", "–")
                .replace("&mdash;", "—")
            val numericDecoded = NUMERIC_ENTITY_REGEX.replace(namedDecoded) { match ->
                val codeRaw = match.groupValues[1]
                val codePoint = if (codeRaw.startsWith("x", ignoreCase = true)) {
                    codeRaw.drop(1).toIntOrNull(16)
                } else {
                    codeRaw.toIntOrNull(10)
                }
                if (codePoint != null && codePoint in 32..0x10FFFF) {
                    String(Character.toChars(codePoint))
                } else {
                    ""
                }
            }
            return numericDecoded.replace(WHITESPACE_REGEX, " ").trim()
        }

        fun interleaveAndDeduplicate(
            byCategory: Map<NewsCategory, List<NewsArticle>>,
            maxTotal: Int = 30,
        ): List<NewsArticle> {
            val order = listOf(NewsCategory.POLITICS, NewsCategory.SPORTS, NewsCategory.WORLD)
            val iterators = order.mapNotNull { cat ->
                byCategory[cat]?.takeIf { it.isNotEmpty() }?.iterator()
            }.toMutableList()
            val seenTitles = HashSet<String>()
            val result = mutableListOf<NewsArticle>()

            while (iterators.isNotEmpty() && result.size < maxTotal) {
                val itList = iterators.iterator()
                while (itList.hasNext() && result.size < maxTotal) {
                    val catIter = itList.next()
                    if (!catIter.hasNext()) {
                        itList.remove()
                        continue
                    }
                    val article = catIter.next()
                    val key = article.title.lowercase().replace(WHITESPACE_REGEX, " ").trim()
                    if (seenTitles.add(key)) {
                        result.add(article)
                    }
                }
            }
            return result
        }
    }
}
