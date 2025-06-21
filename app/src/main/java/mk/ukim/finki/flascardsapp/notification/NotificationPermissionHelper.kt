package mk.ukim.finki.flascardsapp.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(private val context: Context) {

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        const val SCHEDULE_EXACT_ALARM_REQUEST_CODE = 1002
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )) {
                    showPermissionExplanationDialog(activity)
                } else {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun requestExactAlarmPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission()) {
                showExactAlarmPermissionDialog(activity)
            }
        }
    }

    private fun showPermissionExplanationDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Дозвола за нотификации")
            .setMessage("Апликацијата треба дозвола за нотификации за да ве известува кога додавате нови картички и да ве потсетува да учите.")
            .setPositiveButton("Дозволи") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
            .setNegativeButton("Не сега", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun showExactAlarmPermissionDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Дозвола за точни аларми")
            .setMessage("За да ве потсетуваме точно секој час, потребна е дозвола за точни аларми. Дали сакате да ја дозволите?")
            .setPositiveButton("Дозволи") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:${context.packageName}")
                    activity.startActivityForResult(intent, SCHEDULE_EXACT_ALARM_REQUEST_CODE)
                }
            }
            .setNegativeButton("Не сега", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    fun openNotificationSettings(activity: Activity) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }

                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", context.packageName)
                    putExtra("app_uid", context.applicationInfo.uid)
                }
            }
        }
        activity.startActivity(intent)
    }

    fun areAllPermissionsGranted(): Boolean {
        return hasNotificationPermission() && hasExactAlarmPermission()
    }

    fun initializePermissions(activity: Activity) {
        requestNotificationPermission(activity)
        requestExactAlarmPermission(activity)
    }
}