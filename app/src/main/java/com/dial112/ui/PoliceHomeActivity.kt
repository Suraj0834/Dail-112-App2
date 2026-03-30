package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.CaseRepository
import com.dial112.data.repository.SOSRepository
import com.dial112.databinding.ActivityPoliceHomeBinding
import com.dial112.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PoliceHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPoliceHomeBinding
    private lateinit var sosRepository: SOSRepository
    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPoliceHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        sosRepository = SOSRepository(ApiClient.apiService, tokenManager)
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        setupUI()
        setupListeners()
        loadData()
    }

    private fun setupUI() {
        // Set greeting with real officer name
        val user = tokenManager.getUser()
        val officerName = user?.name ?: "Officer"
        binding.tvGreeting.text = getString(R.string.police_greeting, officerName)

        // Setup RecyclerViews
        binding.rvSOSAlerts.apply {
            layoutManager = LinearLayoutManager(
                this@PoliceHomeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        binding.rvAssignedCases.apply {
            layoutManager = LinearLayoutManager(this@PoliceHomeActivity)
        }
    }

    private fun setupListeners() {
        // Status chip toggle
        binding.chipStatus.setOnClickListener {
            toggleDutyStatus()
        }

        // Quick Tools
        binding.btnCriminalSearch?.setOnClickListener {
            navigateToScanner(ScannerMode.FACE)
        }

        binding.btnCriminalAI?.setOnClickListener {
            navigateToScanner(ScannerMode.FACE)
        }

        binding.btnPcrVan?.setOnClickListener {
            // Navigate to PCR Van management
            showMessage("PCR Van Management")
        }

        // View all cases
        binding.btnViewAllCases.setOnClickListener {
            startActivity(Intent(this, CaseManagementActivity::class.java))
        }

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_alerts -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_scanner -> {
                    navigateToScanner(ScannerMode.FACE)
                    true
                }
                R.id.nav_cases -> {
                    startActivity(Intent(this, CaseManagementActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadData() {
        // Load active SOS alerts
        lifecycleScope.launch {
            when (val result = sosRepository.getActiveSOSAlerts()) {
                is Resource.Success -> {
                    val sosAlerts = result.data ?: emptyList()
                    if (sosAlerts.isEmpty()) {
                        binding.tvNoAlerts.visibility = View.VISIBLE
                        binding.rvSOSAlerts.visibility = View.GONE
                    } else {
                        binding.tvNoAlerts.visibility = View.GONE
                        binding.rvSOSAlerts.visibility = View.VISIBLE
                        // Update SOS alerts count
                        binding.tvAlertCount.text = "${sosAlerts.size} Active"
                    }
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load SOS alerts")
                }
                else -> {}
            }
        }

        // Load assigned cases
        lifecycleScope.launch {
            when (val result = caseRepository.getCases()) {
                is Resource.Success -> {
                    val cases = result.data ?: emptyList()
                    if (cases.isEmpty()) {
                        binding.tvNoCases.visibility = View.VISIBLE
                        binding.rvAssignedCases.visibility = View.GONE
                    } else {
                        binding.tvNoCases.visibility = View.GONE
                        binding.rvAssignedCases.visibility = View.VISIBLE
                        // Note: tvCaseCount was causing error because it's not in the binding
                        // We'll update a general status or just rely on the list
                    }
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load cases")
                }
                else -> {}
            }
        }
    }

    private fun toggleDutyStatus() {
        val isOnDuty = binding.chipStatus.text == getString(R.string.police_status_active)
        
        if (isOnDuty) {
            binding.chipStatus.text = getString(R.string.police_status_offline)
            binding.chipStatus.setChipBackgroundColorResource(R.color.text_secondary)
        } else {
            binding.chipStatus.text = getString(R.string.police_status_active)
            binding.chipStatus.setChipBackgroundColorResource(R.color.success_green)
        }
    }

    private fun navigateToScanner(mode: ScannerMode) {
        val intent = Intent(this, CameraScannerActivity::class.java).apply {
            putExtra(EXTRA_SCANNER_MODE, mode.name)
        }
        startActivity(intent)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    enum class ScannerMode {
        FACE, ANPR, WEAPON
    }

    companion object {
        const val EXTRA_SCANNER_MODE = "scanner_mode"
    }
}
