package com.dial112.util

object Constants {
    // Backend API Base URL
    const val BASE_URL = "https://dail-112-be.onrender.com/"

    // Shared Preferences
    const val PREFS_NAME = "dial112_prefs"
    const val KEY_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_USER_PHONE = "user_phone"

    // User Roles
    const val ROLE_CITIZEN = "citizen"
    const val ROLE_POLICE = "police"
    const val ROLE_ADMIN = "admin"

    // Request Timeouts
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Notification IDs
    const val NOTIFICATION_ID_SOS = 1001
    const val NOTIFICATION_ID_GENERAL = 1002
    const val NOTIFICATION_ID_CASE = 1003

    // Intent Extras
    const val EXTRA_CASE_ID = "case_id"
    const val EXTRA_SOS_ID = "sos_id"
    const val EXTRA_SCANNER_MODE = "scanner_mode"

    // Location Update Intervals
    const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
    const val LOCATION_FASTEST_INTERVAL = 2000L // 2 seconds
}
