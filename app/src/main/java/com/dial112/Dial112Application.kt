package com.dial112

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Dial112Application : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val sosChannel = NotificationChannel(
                CHANNEL_ID_SOS,
                "SOS Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Used for active SOS tracking and emergency responses"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(sosChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_SOS = "sos_channel"
    }
}
