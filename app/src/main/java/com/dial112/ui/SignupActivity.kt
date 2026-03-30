package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AuthRepository
import com.dial112.util.Constants
import com.dial112.util.Resource
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var btnBack: ImageButton

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        tokenManager = TokenManager(this)
        authRepository = AuthRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.et_full_name)
        etEmail = findViewById(R.id.et_email)
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        cbTerms = findViewById(R.id.cb_terms)
        btnSignUp = findViewById(R.id.btn_sign_up)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { onBackPressed() }
        btnSignUp.setOnClickListener { handleSignup() }
    }

    private fun handleSignup() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Validation
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isEmpty() || phone.length < 10) {
            Toast.makeText(this, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Please agree to terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button and show loading
        btnSignUp.isEnabled = false
        btnSignUp.text = "Creating Account..."

        // Call API
        lifecycleScope.launch {
            when (val result = authRepository.register(fullName, email, phone, password, Constants.ROLE_CITIZEN)) {
                is Resource.Success -> {
                    Toast.makeText(this@SignupActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to home based on role
                    val intent = Intent(this@SignupActivity, CitizenHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(this@SignupActivity, result.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    btnSignUp.isEnabled = true
                    btnSignUp.text = getString(R.string.sign_up)
                }
                else -> {}
            }
        }
    }
}
