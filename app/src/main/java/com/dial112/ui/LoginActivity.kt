package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AuthRepository
import com.dial112.databinding.ActivityLoginBinding
import com.dial112.util.Constants
import com.dial112.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private var isCitizenLogin = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(ApiClient.apiService, tokenManager)

        // Check if already logged in
        if (authRepository.isLoggedIn()) {
            navigateToHome()
            return
        }

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Set default selection to Citizen
        updateRoleSelection(true)
    }

    private fun setupListeners() {
        // Login type toggle
        binding.btnCitizen.setOnClickListener {
            if (!isCitizenLogin) {
                isCitizenLogin = true
                updateRoleSelection(true)
                updateUIForLoginType()
            }
        }

        binding.btnPolice.setOnClickListener {
            if (isCitizenLogin) {
                isCitizenLogin = false
                updateRoleSelection(false)
                updateUIForLoginType()
            }
        }

        // Password toggle
        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Sign In button
        binding.btnSignIn.setOnClickListener {
            handleSignIn()
        }

        // Forgot password
        binding.btnForgotPassword.setOnClickListener {
            showMessage("Password reset link sent to your email")
        }

        // Biometric login
        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        // Register
        binding.btnRegister.setOnClickListener {
            // Navigate to registration
            showMessage("Registration coming soon")
        }
    }

    private fun updateRoleSelection(isCitizen: Boolean) {
        if (isCitizen) {
            binding.btnCitizen.setBackgroundResource(R.drawable.bg_role_button_selected)
            binding.tvCitizen.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            binding.btnPolice.setBackgroundResource(R.drawable.bg_role_button_unselected)
            binding.tvPolice.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        } else {
            binding.btnPolice.setBackgroundResource(R.drawable.bg_role_button_selected)
            binding.tvPolice.setTextColor(ContextCompat.getColor(this, android.R.color.white))

            binding.btnCitizen.setBackgroundResource(R.drawable.bg_role_button_unselected)
            binding.tvCitizen.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun togglePasswordVisibility() {
        if (binding.etPassword.inputType == 129) { // textPassword
            binding.etPassword.inputType = 1 // text
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility)
        } else {
            binding.etPassword.inputType = 129 // textPassword
            binding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        }
        binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
    }

    private fun updateUIForLoginType() {
        // Update UI elements based on login type - can add additional changes if needed
    }

    private fun handleSignIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validate inputs
        if (email.isEmpty()) {
            showMessage("Email is required")
            binding.etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            showMessage("Password is required")
            binding.etPassword.requestFocus()
            return
        }

        // Show loading state
        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = getString(R.string.loading)

        // Call API
        lifecycleScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    showMessage("Login successful!")
                    navigateToHome()
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Login failed")
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = getString(R.string.login_button)
                }
                else -> {}
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(
                    this,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            navigateToHome()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            showMessage("Authentication error: $errString")
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            showMessage("Authentication failed")
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Login")
                    .setSubtitle("Use your fingerprint to sign in")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                showMessage("Biometric authentication not available")
            }
        }
    }

    private fun navigateToHome() {
        val userRole = tokenManager.getUserRole()
        val intent = when (userRole) {
            Constants.ROLE_POLICE -> Intent(this, PoliceHomeActivity::class.java)
            Constants.ROLE_ADMIN -> Intent(this, AdminDashboardActivity::class.java)
            else -> Intent(this, CitizenHomeActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
