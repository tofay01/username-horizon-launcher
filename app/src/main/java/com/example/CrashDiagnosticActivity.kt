package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File

class CrashDiagnosticActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(HorizonApplication.CRASH_PREFS, Context.MODE_PRIVATE)
        val trace = intent.getStringExtra(EXTRA_TRACE)
            ?: prefs.getString(HorizonApplication.KEY_LAST_CRASH, null)
            ?: "Unknown startup error"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#071832"))
            setPadding(48, 40, 48, 40)
        }

        val title = TextView(this).apply {
            text = "Horizon Launcher — Diagnostic"
            setTextColor(Color.WHITE)
            textSize = 22f
            setTypeface(Typeface.DEFAULT_BOLD)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "تم رصد خطأ أثناء التشغيل / An error was caught during startup:"
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 15f
            setPadding(0, 12, 0, 20)
        }
        root.addView(subtitle)

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            setPadding(0, 0, 0, 20)
        }

        val retryBtn = Button(this).apply {
            text = "إعادة المحاولة / Retry"
            isFocusable = true
            setOnClickListener {
                startActivity(
                    Intent(this@CrashDiagnosticActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                finish()
            }
        }
        btnRow.addView(retryBtn)

        val resetBtn = Button(this).apply {
            text = "إعادة ضبط الإعدادات / Reset Data"
            isFocusable = true
            setOnClickListener {
                runCatching {
                    File(filesDir, "datastore").deleteRecursively()
                }
                prefs.edit().clear().commit()
                startActivity(
                    Intent(this@CrashDiagnosticActivity, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                )
                finish()
            }
        }
        btnRow.addView(resetBtn)
        root.addView(btnRow)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
            setBackgroundColor(Color.parseColor("#0B234A"))
            setPadding(24, 24, 24, 24)
        }

        val traceView = TextView(this).apply {
            text = trace
            setTextColor(Color.parseColor("#7DD3FC"))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        scroll.addView(traceView)
        root.addView(scroll)

        setContentView(root)
        retryBtn.requestFocus()
    }

    companion object {
        const val EXTRA_TRACE = "extra_trace"
    }
}
