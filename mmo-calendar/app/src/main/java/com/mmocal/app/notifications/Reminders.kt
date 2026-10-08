package com.mmocal.app.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mmocal.app.R
import com.mmocal.app.data.EventType
import com.mmocal.app.data.GameEvent
import com.mmocal.app.data.GamesRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object Reminders {
    const val CHANNEL_ID = "release_reminders"
    private const val ACTION_REMIND = "com.mmocal.app.action.REMIND"
    private const val REQUEST_CODE = 4100
    private const val DAYS_BEFORE = 3L

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Напоминания о релизах",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Уведомления за 3 дня до выхода игр"
                    }
                )
            }
        }
    }

    fun schedule(context: Context, favorites: Set<String> = emptySet()) {
        cancel(context)
        val event = nextTarget(favorites) ?: return
        val date = event.date ?: return
        val fireAt = LocalDateTime.of(date.minusDays(DAYS_BEFORE), LocalTime.NOON)
        if (!fireAt.isAfter(LocalDateTime.now())) return

        val triggerAt = fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(context, event, favorites)
        )
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, null, emptySet()))
    }

    private fun nextTarget(favorites: Set<String>): GameEvent? {
        val today = LocalDate.now()
        val upcoming = GamesRepository.upcoming(today).filter { it.date != null }
        return upcoming.firstOrNull { it.id in favorites }
            ?: upcoming.firstOrNull { it.type == EventType.LAUNCH && it.confirmed }
    }

    private fun pendingIntent(
        context: Context,
        event: GameEvent?,
        favorites: Set<String>
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND
            event?.let {
                putExtra("event_id", it.id)
                putExtra("title", it.title)
                putExtra("subtitle", it.windowText)
            }
            putStringArrayListExtra("favorites", ArrayList(favorites))
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        if (title != null) {
            Reminders.ensureChannel(context)
            val subtitle = intent.getStringExtra("subtitle").orEmpty()
            val notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_calendar)
                .setContentTitle("Скоро релиз: $title")
                .setContentText("$subtitle · через $DAYS_BEFORE дня")
                .setAutoCancel(true)
                .build()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            runCatching {
                manager.notify(
                    intent.getStringExtra("event_id").orEmpty().hashCode(),
                    notification
                )
            }
        }
        val favorites = intent.getStringArrayListExtra("favorites")?.toSet().orEmpty()
        Reminders.schedule(context, favorites)
    }

    private companion object {
        const val DAYS_BEFORE = 3
    }
}
