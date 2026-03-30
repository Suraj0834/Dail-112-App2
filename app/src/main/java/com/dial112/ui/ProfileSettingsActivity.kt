package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AuthRepository

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnEditProfile: Button
    private lateinit var swNotifications: Switch
    private lateinit var swDarkMode: Switch
    private lateinit var btnLogout: Button

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        tokenManager = TokenManager(this)
        authRepository = AuthRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        loadUserProfile()
        setupListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        swNotifications = findViewById(R.id.sw_notifications)
        swDarkMode = findViewById(R.id.sw_dark_mode)
        btnLogout = findViewById(R.id.btn_logout)
    }

    private fun loadUserProfile() {
        val user = tokenManager.getUser()
        user?.let {
            // Update UI with user data
            findViewById<TextView>(R.id.tv_user_name)?.text = it.name
            findViewById<TextView>(R.id.tv_user_email)?.text = it.email
            findViewById<TextView>(R.id.tv_user_phone)?.text = it.phone
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { onBackPressed() }

        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit profile feature coming soon", Toast.LENGTH_SHORT).show()
        }

        swNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Notifications $status", Toast.LENGTH_SHORT).show()
        }

        swDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Dark mode $status", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Clear session using AuthRepository
        authRepository.logout()

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
