package mk.ukim.finki.flascardsapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import mk.ukim.finki.flascardsapp.MainActivity
import mk.ukim.finki.flascardsapp.R

class FlashcardNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_NEW_CARD = "new_flashcard_channel"
        const val CHANNEL_ID_STUDY_REMINDER = "study_reminder_channel"
        const val NOTIFICATION_ID_NEW_CARD = 1001
        const val NOTIFICATION_ID_STUDY_REMINDER = 1002

        private const val CHANNEL_NAME_NEW_CARD = "Нови картички"
        private const val CHANNEL_DESC_NEW_CARD = "Нотификации за нови додадени картички"
        private const val CHANNEL_NAME_STUDY_REMINDER = "Потсетување за учење"
        private const val CHANNEL_DESC_STUDY_REMINDER = "Потсетување да се врати на апликацијата за учење"
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val newCardChannel = NotificationChannel(
                CHANNEL_ID_NEW_CARD,
                CHANNEL_NAME_NEW_CARD,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_NEW_CARD
                enableLights(true)
                enableVibration(true)
            }

            val studyReminderChannel = NotificationChannel(
                CHANNEL_ID_STUDY_REMINDER,
                CHANNEL_NAME_STUDY_REMINDER,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC_STUDY_REMINDER
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(newCardChannel)
            manager.createNotificationChannel(studyReminderChannel)
        }
    }

    fun showNewCardNotification(question: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val shortQuestion = if (question.length > 50) {
            question.substring(0, 47) + "..."
        } else {
            question
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_NEW_CARD)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Нова картичка додадена!")
            .setContentText("Прашање: $shortQuestion")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Успешно додадовте нова картичка:\n\n\"$question\"\n\nПочнете да учите сега!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_NEW_CARD, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showStudyReminderNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = arrayOf(
            "Време е за учење!",
            "Вашите картички ве чекаат!",
            "Направете прогрес денес!",
            "Учете малку секој ден!",
            "Знаењето е сила!"
        )

        val randomMessage = messages.random()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STUDY_REMINDER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Потсетување за учење")
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$randomMessage\n\nОтворете ја апликацијата и продолжете со учењето на вашите картички!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_study,
                "Учи сега",
                pendingIntent
            )
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_STUDY_REMINDER, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}