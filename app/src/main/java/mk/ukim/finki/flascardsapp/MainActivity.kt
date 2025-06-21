package mk.ukim.finki.flascardsapp

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import mk.ukim.finki.flascardsapp.notification.NotificationPermissionHelper
import mk.ukim.finki.flascardsapp.notification.StudyReminderScheduler

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionHelper: NotificationPermissionHelper
    private lateinit var studyReminderScheduler: StudyReminderScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationPermissionHelper = NotificationPermissionHelper(this)
        studyReminderScheduler = StudyReminderScheduler(this)

        notificationPermissionHelper.initializePermissions(this)

        if (studyReminderScheduler.areRemindersEnabled()) {
            studyReminderScheduler.scheduleHourlyReminders()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NotificationPermissionHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (notificationPermissionHelper.hasNotificationPermission()) {

            } else {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NotificationPermissionHelper.SCHEDULE_EXACT_ALARM_REQUEST_CODE) {
            if (notificationPermissionHelper.hasExactAlarmPermission()) {
                if (studyReminderScheduler.areRemindersEnabled()) {
                    studyReminderScheduler.scheduleHourlyReminders()
                }
            } else {

            }
        }
    }
}