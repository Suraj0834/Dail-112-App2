package com.dial112.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AuthRepository
import com.dial112.data.repository.CaseRepository
import com.dial112.databinding.ActivityCitizenHomeBinding
import com.dial112.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CitizenHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitizenHomeBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitizenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        authRepository = AuthRepository(ApiClient.apiService, tokenManager)
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        setupUI()
        setupListeners()
        loadData()
    }

    private fun setupUI() {
        // Set greeting with real user name
        val user = tokenManager.getUser()
        val userName = user?.name ?: "Citizen"
        binding.tvGreeting.text = getString(R.string.home_greeting, userName)

        // Setup RecyclerView for recent cases
        binding.rvRecentCases.apply {
            layoutManager = LinearLayoutManager(this@CitizenHomeActivity)
        }
    }

    private fun setupListeners() {
        // SOS Card click
        binding.cardSOS.setOnClickListener {
            navigateToSOS()
        }

        // Floating SOS button
        binding.fabSOS.setOnClickListener {
            navigateToSOS()
        }

        // Quick Actions
        binding.btnFileFir?.setOnClickListener {
            // Navigate to File FIR
            showMessage("File FIR")
        }

        binding.btnMyCases?.setOnClickListener {
            startActivity(Intent(this, CaseHistoryActivity::class.java))
        }

        binding.btnCrimeMap?.setOnClickListener {
            startActivity(Intent(this, MapLocationActivity::class.java))
        }

        binding.btnAiChat?.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // View all cases
        binding.btnViewAll.setOnClickListener {
            startActivity(Intent(this, CaseHistoryActivity::class.java))
        }

        // Header actions
        binding.btnNotifications?.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.btnProfile?.setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        // Bottom navigation
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_cases -> {
                    startActivity(Intent(this, CaseHistoryActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatActivity::class.java))
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
        lifecycleScope.launch {
            when (val result = caseRepository.getCases()) {
                is Resource.Success -> {
                    val cases = result.data ?: emptyList()
                    // Take only the first 5 cases for recent cases
                    val recentCases = cases.take(5)

                    if (recentCases.isEmpty()) {
                        binding.layoutEmptyState?.visibility = View.VISIBLE
                        binding.rvRecentCases.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState?.visibility = View.GONE
                        binding.rvRecentCases.visibility = View.VISIBLE
                        // Adapter will be set when RecyclerView adapter is implemented
                    }
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load cases")
                    binding.layoutEmptyState?.visibility = View.VISIBLE
                    binding.rvRecentCases.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    private fun navigateToSOS() {
        startActivity(Intent(this, SOSActivity::class.java))
    }

    private fun callEmergencyNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
