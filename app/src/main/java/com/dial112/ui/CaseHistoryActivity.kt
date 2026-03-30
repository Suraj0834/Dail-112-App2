package com.dial112.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.model.response.Case
import com.dial112.data.repository.CaseRepository
import com.dial112.util.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CaseHistoryActivity : AppCompatActivity() {

    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager

    // Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnSort: ImageButton
    private lateinit var btnAdvancedFilter: ImageButton
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var etSearchCases: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var rvCases: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvResultsCount: TextView

    // Stats
    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatActive: TextView
    private lateinit var tvStatResolved: TextView

    // Filter chips
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipActive: Chip
    private lateinit var chipResolved: Chip
    private lateinit var chipClosed: Chip

    private var allCases: List<Case> = emptyList()
    private var filteredCases: List<Case> = emptyList()
    private var currentFilter = "all"
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_history)

        tokenManager = TokenManager(this)
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        setupListeners()
        setupRecyclerView()
        loadCases()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btn_back)
        btnSort = findViewById(R.id.btn_sort)
        btnAdvancedFilter = findViewById(R.id.btn_advanced_filter)
        swipeRefresh = findViewById(R.id.swipe_refresh)
        etSearchCases = findViewById(R.id.et_search_cases)
        ivClearSearch = findViewById(R.id.iv_clear_search)
        rvCases = findViewById(R.id.rv_cases)
        emptyState = findViewById(R.id.empty_state)
        tvResultsCount = findViewById(R.id.tv_results_count)

        // Stats
        tvStatTotal = findViewById(R.id.tv_stat_total)
        tvStatActive = findViewById(R.id.tv_stat_active)
        tvStatResolved = findViewById(R.id.tv_stat_resolved)

        // Filter chips
        chipAll = findViewById(R.id.chip_all)
        chipPending = findViewById(R.id.chip_pending)
        chipActive = findViewById(R.id.chip_active)
        chipResolved = findViewById(R.id.chip_resolved)
        chipClosed = findViewById(R.id.chip_closed)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnSort.setOnClickListener {
            showMessage("Sort options coming soon")
        }

        btnAdvancedFilter.setOnClickListener {
            showMessage("Advanced filters coming soon")
        }

        // Pull to refresh
        swipeRefresh.setOnRefreshListener {
            loadCases()
        }

        // Search functionality
        etSearchCases.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                ivClearSearch.visibility = if (searchQuery.isEmpty()) View.GONE else View.VISIBLE
                applyFilters()
            }
        })

        ivClearSearch.setOnClickListener {
            etSearchCases.text.clear()
        }

        // Filter chips
        chipAll.setOnClickListener {
            currentFilter = "all"
            applyFilters()
        }

        chipPending.setOnClickListener {
            currentFilter = "pending"
            applyFilters()
        }

        chipActive.setOnClickListener {
            currentFilter = "active"
            applyFilters()
        }

        chipResolved.setOnClickListener {
            currentFilter = "resolved"
            applyFilters()
        }

        chipClosed.setOnClickListener {
            currentFilter = "closed"
            applyFilters()
        }
    }

    private fun setupRecyclerView() {
        rvCases.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCases() {
        lifecycleScope.launch {
            swipeRefresh.isRefreshing = true

            when (val result = caseRepository.getCases()) {
                is Resource.Success -> {
                    allCases = result.data ?: emptyList()
                    updateStats()
                    applyFilters()
                    swipeRefresh.isRefreshing = false
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load cases")
                    swipeRefresh.isRefreshing = false
                }
                else -> {
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun updateStats() {
        val totalCount = allCases.size
        val activeCount = allCases.count {
            val status = it.status?.lowercase() ?: ""
            status == "active" || status == "open" || status == "pending"
        }
        val resolvedCount = allCases.count {
            val status = it.status?.lowercase() ?: ""
            status == "resolved" || status == "closed"
        }

        tvStatTotal.text = totalCount.toString()
        tvStatActive.text = activeCount.toString()
        tvStatResolved.text = resolvedCount.toString()
    }

    private fun applyFilters() {
        // Filter by status
        var filtered = when (currentFilter) {
            "pending" -> allCases.filter { it.status?.lowercase() == "pending" }
            "active" -> allCases.filter {
                val status = it.status?.lowercase() ?: ""
                status == "active" || status == "open"
            }
            "resolved" -> allCases.filter { it.status?.lowercase() == "resolved" }
            "closed" -> allCases.filter { it.status?.lowercase() == "closed" }
            else -> allCases // "all"
        }

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                (it.title?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (it.description?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (it.category?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (it.location?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (it.id?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }

        filteredCases = filtered
        updateUI()
    }

    private fun updateUI() {
        // Update results count
        tvResultsCount.text = "Showing ${filteredCases.size} cases"

        // Show/hide empty state
        if (filteredCases.isEmpty()) {
            rvCases.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvCases.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
            // Set adapter here when CaseAdapter is implemented
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
