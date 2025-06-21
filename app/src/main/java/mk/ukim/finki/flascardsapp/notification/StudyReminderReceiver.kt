package mk.ukim.finki.flascardsapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*


class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("StudyReminder", "Study reminder triggered")

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        if (currentHour in 8..22) {
            val notificationManager = FlashcardNotificationManager(context)
            notificationManager.showStudyReminderNotification()
        }
    }
}

class StudyReminderScheduler(private val context: Context) {

    companion object {
        private const val STUDY_REMINDER_REQUEST_CODE = 2001
        private const val PREFS_NAME = "flashcard_prefs"
        private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleHourlyReminders() {
        if (!areRemindersEnabled()) {
            Log.d("StudyReminder", "Reminders are disabled")
            return
        }

        val intent = Intent(context, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STUDY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_HOUR,
                pendingIntent
            )

            Log.d("StudyReminder", "Hourly reminders scheduled starting at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("StudyReminder", "Failed to schedule alarms - missing permission", e)
        }
    }

    fun cancelHourlyReminders() {
        val intent = Intent(context, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STUDY_REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("StudyReminder", "Hourly reminders cancelled")
    }

    fun setRemindersEnabled(enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()

        if (enabled) {
            scheduleHourlyReminders()
        } else {
            cancelHourlyReminders()
        }
    }

    fun areRemindersEnabled(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true) // Default enabled
    }

    fun scheduleTestReminder(intervalMinutes: Int = 1) {
        val intent = Intent(context, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            STUDY_REMINDER_REQUEST_CODE + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, intervalMinutes)
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                (intervalMinutes * 60 * 1000).toLong(),
                pendingIntent
            )

            Log.d("StudyReminder", "Test reminder scheduled every $intervalMinutes minutes")
        } catch (e: SecurityException) {
            Log.e("StudyReminder", "Failed to schedule test reminder", e)
        }
    }
}