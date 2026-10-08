package com.mmocal.app.ui.components

import android.content.Context
import android.content.Intent
import com.mmocal.app.data.GameEvent
import com.mmocal.app.data.formatDate

fun shareEvent(context: Context, event: GameEvent) {
    val text = buildString {
        append(event.title)
        append(" — ")
        append(event.windowText)
        if (event.date != null) {
            append(" (")
            append(formatDate(event.date))
            append(")")
        }
        append("\n")
        append(event.developer)
        append("\nMMO Calendar")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Поделиться"))
}
