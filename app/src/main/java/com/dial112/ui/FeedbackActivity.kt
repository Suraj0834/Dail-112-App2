package com.dial112.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dial112.R

class FeedbackActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var btnRate1: FrameLayout
    private lateinit var btnRate2: FrameLayout
    private lateinit var btnRate3: FrameLayout
    private lateinit var btnRate4: FrameLayout
    private lateinit var btnRate5: FrameLayout
    private lateinit var spinnerFeedbackType: Spinner
    private lateinit var etFeedback: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSubmit: Button

    private var selectedRating = 0
    private val ratingButtons = mutableListOf<FrameLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        initializeViews()
        setupSpinner()
        setupListeners()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        btnRate1 = findViewById(R.id.btnRate1)
        btnRate2 = findViewById(R.id.btnRate2)
        btnRate3 = findViewById(R.id.btnRate3)
        btnRate4 = findViewById(R.id.btnRate4)
        btnRate5 = findViewById(R.id.btnRate5)
        spinnerFeedbackType = findViewById(R.id.spinnerFeedbackType)
        etFeedback = findViewById(R.id.etFeedback)
        etEmail = findViewById(R.id.etEmail)
        btnSubmit = findViewById(R.id.btnSubmit)

        ratingButtons.addAll(listOf(btnRate1, btnRate2, btnRate3, btnRate4, btnRate5))
    }

    private fun setupSpinner() {
        val feedbackTypes = arrayOf(
            "General Feedback",
            "Bug Report",
            "Feature Request",
            "Complaint",
            "Suggestion",
            "Other"
        )
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            feedbackTypes
        )
        spinnerFeedbackType.adapter = adapter
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        // Rating button listeners
        btnRate1.setOnClickListener { selectRating(1) }
        btnRate2.setOnClickListener { selectRating(2) }
        btnRate3.setOnClickListener { selectRating(3) }
        btnRate4.setOnClickListener { selectRating(4) }
        btnRate5.setOnClickListener { selectRating(5) }

        btnSubmit.setOnClickListener { submitFeedback() }
    }

    private fun selectRating(rating: Int) {
        selectedRating = rating

        // Reset all rating buttons
        ratingButtons.forEach { button ->
            button.setBackgroundResource(R.drawable.bg_rating_circle)
        }

        // Highlight selected rating and all ratings below it
        for (i in 0 until rating) {
            ratingButtons[i].setBackgroundResource(R.drawable.bg_rating_circle_selected)
        }
    }

    private fun submitFeedback() {
        val feedbackType = spinnerFeedbackType.selectedItem.toString()
        val feedback = etFeedback.text.toString().trim()
        val email = etEmail.text.toString().trim()

        if (selectedRating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        if (feedback.isEmpty()) {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show()
            return
        }

        // Email is optional, so we don't validate it

        // Submit feedback to backend
        // For now, just show success message
        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
        finish()
    }
}
