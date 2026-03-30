package com.dial112.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dial112.R

class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var tvSubtitle: TextView
    private lateinit var otpFields: Array<EditText>
    private lateinit var btnVerify: Button
    private lateinit var btnResend: TextView
    private lateinit var tvTimer: TextView
    private var isTimerRunning = false
    private var phoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        phoneNumber = intent.getStringExtra("phone_number")

        initializeViews()
        setupListeners()
        updateSubtitle()
        startTimer()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        otpFields = arrayOf(
            findViewById(R.id.et_otp_1),
            findViewById(R.id.et_otp_2),
            findViewById(R.id.et_otp_3),
            findViewById(R.id.et_otp_4),
            findViewById(R.id.et_otp_5),
            findViewById(R.id.et_otp_6)
        )
        btnVerify = findViewById(R.id.btn_verify)
        btnResend = findViewById(R.id.btn_resend)
        tvTimer = findViewById(R.id.tv_timer)
    }

    private fun updateSubtitle() {
        if (phoneNumber != null) {
            tvSubtitle.text = "Enter the 6-digit code sent to\n$phoneNumber"
        }
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        // Setup OTP field navigation
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
            })

            // Handle backspace to move to previous field
            otpFields[i].setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    otpFields[i].text.isEmpty() && i > 0) {
                    otpFields[i - 1].requestFocus()
                    otpFields[i - 1].setSelection(otpFields[i - 1].text.length)
                }
                false
            }
        }

        btnVerify.setOnClickListener { verifyOTP() }
        btnResend.setOnClickListener { resendOTP() }
    }

    private fun verifyOTP() {
        val otp = otpFields.joinToString("") { it.text.toString() }
        if (otp.length != 6) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Verify OTP with backend
        // For now, simulate verification
        Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()

        // Navigate to home screen
        startActivity(android.content.Intent(this, CitizenHomeActivity::class.java))
        finish()
    }

    private fun resendOTP() {
        if (!isTimerRunning) {
            // TODO: Call backend to resend OTP
            Toast.makeText(this, "OTP resent to your number", Toast.LENGTH_SHORT).show()
            clearOTPFields()
            startTimer()
        } else {
            Toast.makeText(this, "Please wait before requesting a new code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearOTPFields() {
        otpFields.forEach { it.text.clear() }
        otpFields[0].requestFocus()
    }

    private fun startTimer() {
        isTimerRunning = true
        btnResend.isEnabled = false
        btnResend.alpha = 0.5f

        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Resend in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                isTimerRunning = false
                btnResend.isEnabled = true
                btnResend.alpha = 1.0f
                tvTimer.text = "Resend code now"
            }
        }.start()
    }
}
