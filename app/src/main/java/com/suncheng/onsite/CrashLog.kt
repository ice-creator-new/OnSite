package com.suncheng.onsite

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashLog {
    private const val TAG = "OnSiteCrash"
    private const val FILE = "last_crash.txt"

    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            write(context, error)
            previous?.uncaughtException(thread, error)
        }
    }

    fun write(context: Context, error: Throwable) {
        val text = buildString {
            appendLine(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            appendLine(error.stackTraceToString())
        }
        Log.e(TAG, text, error)
        runCatching {
            File(context.filesDir, FILE).writeText(text)
        }
    }

    fun read(context: Context): String? {
        return runCatching { File(context.filesDir, FILE).takeIf { it.exists() }?.readText() }.getOrNull()
    }
}
