package com.dial112.ui

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.model.response.Case
import com.dial112.data.repository.CaseRepository
import com.dial112.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CaseDetailsActivity : AppCompatActivity() {

    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnShare: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var tvCaseId: TextView
    private lateinit var tvCaseTitle: TextView
    private lateinit var tvCaseStatus: TextView
    private lateinit var tvCasePriority: TextView
    private lateinit var tvCaseTime: TextView
    private lateinit var tvCaseCategory: TextView
    private lateinit var tvCaseLocation: TextView
    private lateinit var tvCaseDate: TextView
    private lateinit var tvReportedBy: TextView
    private lateinit var tvCaseDescription: TextView
    private lateinit var tvOfficerName: TextView
    private lateinit var tvOfficerBadge: TextView
    private lateinit var statusBadge: LinearLayout
    private lateinit var priorityBadge: LinearLayout

    // Action buttons
    private lateinit var btnUpdateStatus: LinearLayout
    private lateinit var btnAddNote: LinearLayout
    private lateinit var btnViewMap: LinearLayout
    private lateinit var btnCallOfficer: ImageButton
    private lateinit var btnMessageOfficer: ImageButton

    private var currentCase: Case? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_details)

        tokenManager = TokenManager(this)
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        setupListeners()
        loadCaseDetails()
    }

    private fun initializeViews() {
        // Header buttons
        btnBack = findViewById(R.id.btn_back)
        btnShare = findViewById(R.id.btn_share)
        btnMore = findViewById(R.id.btn_more)

        // Case info
        tvCaseId = findViewById(R.id.tv_case_id)
        tvCaseTitle = findViewById(R.id.tv_case_title)
        tvCaseStatus = findViewById(R.id.tv_case_status)
        tvCasePriority = findViewById(R.id.tv_case_priority)
        tvCaseTime = findViewById(R.id.tv_case_time)
        tvCaseCategory = findViewById(R.id.tv_case_category)
        tvCaseLocation = findViewById(R.id.tv_case_location)
        tvCaseDate = findViewById(R.id.tv_case_date)
        tvReportedBy = findViewById(R.id.tv_reported_by)
        tvCaseDescription = findViewById(R.id.tv_case_description)

        // Officer info
        tvOfficerName = findViewById(R.id.tv_officer_name)
        tvOfficerBadge = findViewById(R.id.tv_officer_badge)

        // Badges
        statusBadge = findViewById(R.id.status_badge)
        priorityBadge = findViewById(R.id.priority_badge)

        // Action buttons
        btnUpdateStatus = findViewById(R.id.btn_update_status)
        btnAddNote = findViewById(R.id.btn_add_note)
        btnViewMap = findViewById(R.id.btn_view_map)
        btnCallOfficer = findViewById(R.id.btn_call_officer)
        btnMessageOfficer = findViewById(R.id.btn_message_officer)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnShare.setOnClickListener {
            shareCase()
        }

        btnMore.setOnClickListener {
            showMessage("More options coming soon")
        }

        btnUpdateStatus.setOnClickListener {
            showMessage("Update status coming soon")
        }

        btnAddNote.setOnClickListener {
            showMessage("Add note coming soon")
        }

        btnViewMap.setOnClickListener {
            openMapLocation()
        }

        btnCallOfficer.setOnClickListener {
            showMessage("Call officer: +1-XXX-XXX-XXXX")
        }

        btnMessageOfficer.setOnClickListener {
            showMessage("Message officer coming soon")
        }
    }

    private fun loadCaseDetails() {
        val caseId = intent.getStringExtra("case_id") ?: ""

        if (caseId.isEmpty()) {
            showMessage("Invalid case ID")
            finish()
            return
        }

        lifecycleScope.launch {
            when (val result = caseRepository.getCaseById(caseId)) {
                is Resource.Success -> {
                    val case = result.data
                    if (case != null) {
                        currentCase = case
                        populateUI(case)
                    } else {
                        showMessage("Case not found")
                        finish()
                    }
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load case details")
                    finish()
                }
                else -> {}
            }
        }
    }

    private fun populateUI(case: Case) {
        // Case ID and Title
        tvCaseId.text = "CASE #${case.id?.takeLast(8)?.uppercase() ?: "UNKNOWN"}"
        tvCaseTitle.text = case.title ?: "No Title"

        // Status badge
        updateStatusBadge(case.status ?: "pending")

        // Priority badge
        tvCasePriority.text = case.priority?.uppercase() ?: "MEDIUM"
        priorityBadge.visibility = View.VISIBLE

        // Time ago
        tvCaseTime.text = formatTimeAgo(case.createdAt ?: "")

        // Case information
        tvCaseCategory.text = case.category ?: "Uncategorized"
        tvCaseLocation.text = case.location ?: "No Location"
        tvCaseDate.text = formatDate(case.createdAt ?: "")
        tvReportedBy.text = "User ${case.userId?.takeLast(4) ?: "Unknown"}"

        // Description
        tvCaseDescription.text = case.description ?: "No description provided."

        // Officer info
        if (case.assignedOfficer != null) {
            tvOfficerName.text = "Officer ${case.assignedOfficer.name ?: case.assignedOfficer.id?.takeLast(4) ?: "Unknown"}"
            tvOfficerBadge.text = "Badge #${case.assignedOfficer.badgeId ?: "N/A"} • On Duty"
            btnCallOfficer.isEnabled = true
            btnMessageOfficer.isEnabled = true
        } else {
            tvOfficerName.text = "Not Assigned"
            tvOfficerBadge.text = "Waiting for assignment"
            btnCallOfficer.isEnabled = false
            btnMessageOfficer.isEnabled = false
        }
    }

    private fun updateStatusBadge(status: String) {
        val statusLower = status.lowercase()
        tvCaseStatus.text = status.uppercase()

        val (backgroundRes, textColor) = when (statusLower) {
            "active", "open", "assigned" -> {
                R.drawable.bg_status_badge_active to "#F97316"
            }
            "resolved" -> {
                R.drawable.bg_status_badge_resolved to "#10B981"
            }
            "pending", "new" -> {
                R.drawable.bg_status_badge_pending to "#EAB308"
            }
            "closed" -> {
                R.drawable.bg_status_badge_closed to "#9E9E9E"
            }
            else -> {
                R.drawable.bg_status_badge_pending to "#EAB308"
            }
        }

        statusBadge.setBackgroundResource(backgroundRes)
        tvCaseStatus.setTextColor(android.graphics.Color.parseColor(textColor))
    }

    private fun formatTimeAgo(dateString: String): String {
        if (dateString.isEmpty()) return "Unknown"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateString)

            val now = System.currentTimeMillis()
            val diff = now - (date?.time ?: now)

            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes mins ago"
                hours < 24 -> "$hours hours ago"
                days == 1L -> "Yesterday"
                days < 7 -> "$days days ago"
                else -> formatDate(dateString)
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun formatDate(dateString: String): String {
        if (dateString.isEmpty()) return "Unknown"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateString)

            val outputFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    private fun shareCase() {
        val case = currentCase ?: return

        val shareText = """
            Case Details
            ━━━━━━━━━━━━━━━━
            Case ID: ${case.id ?: "N/A"}
            Title: ${case.title ?: "N/A"}
            Category: ${case.category ?: "N/A"}
            Status: ${case.status ?: "N/A"}
            Location: ${case.location ?: "N/A"}

            Description:
            ${case.description ?: "N/A"}

            Shared from Dial 112 App
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Case #${case.id?.takeLast(8) ?: "UNKNOWN"} - ${case.title ?: "Details"}")
        }

        startActivity(Intent.createChooser(shareIntent, "Share Case via"))
    }

    private fun openMapLocation() {
        val case = currentCase ?: return

        if (case.latitude != null && case.longitude != null) {
            val gmmIntentUri = Uri.parse("geo:${case.latitude},${case.longitude}?q=${case.latitude},${case.longitude}(${case.title})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                showMessage("Google Maps not installed")
            }
        } else {
            showMessage("Location coordinates not available")
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
