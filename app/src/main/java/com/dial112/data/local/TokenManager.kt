package com.dial112.data.local

import android.content.Context
import android.content.SharedPreferences
import com.dial112.data.model.response.User
import com.dial112.util.Constants

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString(Constants.KEY_USER_ID, user.id)
            putString(Constants.KEY_USER_NAME, user.name)
            putString(Constants.KEY_USER_EMAIL, user.email)
            putString(Constants.KEY_USER_ROLE, user.role)
            putString(Constants.KEY_USER_PHONE, user.phone)
            apply()
        }
    }

    fun getUser(): User? {
        val id = prefs.getString(Constants.KEY_USER_ID, null) ?: return null
        val name = prefs.getString(Constants.KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(Constants.KEY_USER_EMAIL, "") ?: ""
        val role = prefs.getString(Constants.KEY_USER_ROLE, "") ?: ""
        val phone = prefs.getString(Constants.KEY_USER_PHONE, "") ?: ""

        return User(id, name, email, phone, role)
    }

    fun getUserId(): String? {
        return prefs.getString(Constants.KEY_USER_ID, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(Constants.KEY_USER_ROLE, null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
