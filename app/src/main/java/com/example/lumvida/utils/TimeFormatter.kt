package com.example.lumvida.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeFormatter {
    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffSeconds = (now - timestamp) / 1000

        return when {
            diffSeconds < 60 -> "$diffSeconds seg"
            diffSeconds < 3600 -> "${diffSeconds / 60} min"
            diffSeconds < 86400 -> "${diffSeconds / 3600} hr"
            diffSeconds < 604800 -> "${diffSeconds / 86400} d"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale("es")).format(Date(timestamp))
        }
    }
}