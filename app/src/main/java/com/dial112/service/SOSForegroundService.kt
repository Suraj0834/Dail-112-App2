package com.dial112.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.dial112.Dial112Application
import com.dial112.R
import com.dial112.ui.SOSActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Foreground Service for tracking location during SOS emergency
 * Provides real-time location updates to emergency responders
 */
class SOSForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var isTracking = false

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SOS -> {
                startForegroundService()
                startLocationTracking()
            }
            ACTION_STOP_SOS -> {
                stopLocationTracking()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(): Notification {
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Dial112Application.CHANNEL_ID_SOS,
                "SOS Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Location tracking during SOS emergency"
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Intent to open SOS activity when notification is tapped
        val notificationIntent = Intent(this, SOSActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        return NotificationCompat.Builder(this, Dial112Application.CHANNEL_ID_SOS)
            .setContentTitle("SOS Active")
            .setContentText("Sharing your location with emergency services")
            .setSmallIcon(R.drawable.ic_sos)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }
    }

    private fun startLocationTracking() {
        if (isTracking) return

        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        // Create location request for high-accuracy updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
            setWaitForAccurateLocation(false)
        }.build()

        // Start receiving location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        isTracking = true
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }

    private fun handleLocationUpdate(location: Location) {
        // TODO: Send location update to backend API
        // This would typically:
        // 1. Send location to server via API
        // 2. Update local broadcast for UI updates
        // 3. Store location history

        // For now, just log (in production, send to backend)
        android.util.Log.d(
            TAG,
            "Location Update: ${location.latitude}, ${location.longitude}"
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
    }

    companion object {
        private const val TAG = "SOSForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
        private const val LOCATION_FASTEST_INTERVAL = 2000L // 2 seconds

        const val ACTION_START_SOS = "com.dial112.action.START_SOS"
        const val ACTION_STOP_SOS = "com.dial112.action.STOP_SOS"
    }
}
