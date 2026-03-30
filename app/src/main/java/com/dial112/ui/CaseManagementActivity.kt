package com.dial112.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.model.response.Case
import com.dial112.data.repository.CaseRepository
import com.dial112.ui.adapter.CaseManagementAdapter
import com.dial112.util.Resource
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CaseManagementActivity : AppCompatActivity() {

    // Views
    private lateinit var ivBack: ImageView
    private lateinit var etSearchCases: EditText
    private lateinit var ivClearSearch: ImageView
    private lateinit var chipAll: Chip
    private lateinit var chipNew: Chip
    private lateinit var chipUnassigned: Chip
    private lateinit var chipAssigned: Chip
    private lateinit var chipMyCases: Chip
    private lateinit var tvUnassignedCount: TextView
    private lateinit var tvInProgressCount: TextView
    private lateinit var tvMyCasesCount: TextView
    private lateinit var tvResultsCount: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var rvCases: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout

    // Data
    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var adapter: CaseManagementAdapter

    private var allCases: List<Case> = emptyList()
    private var filteredCases: List<Case> = emptyList()
    private var currentFilter = "all"
    private var searchQuery = ""
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_case_management)

        tokenManager = TokenManager(this)
        currentUserId = tokenManager.getUserId()
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        setupListeners()
        setupRecyclerView()
        loadCases()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        etSearchCases = findViewById(R.id.etSearchCases)
        ivClearSearch = findViewById(R.id.ivClearSearch)
        chipAll = findViewById(R.id.chipAll)
        chipNew = findViewById(R.id.chipNew)
        chipUnassigned = findViewById(R.id.chipUnassigned)
        chipAssigned = findViewById(R.id.chipAssigned)
        chipMyCases = findViewById(R.id.chipMyCases)
        tvUnassignedCount = findViewById(R.id.tvUnassignedCount)
        tvInProgressCount = findViewById(R.id.tvInProgressCount)
        tvMyCasesCount = findViewById(R.id.tvMyCasesCount)
        tvResultsCount = findViewById(R.id.tvResultsCount)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        rvCases = findViewById(R.id.rvCases)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

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
            etSearchCases.setText("")
        }

        // Filter chips
        chipAll.setOnClickListener {
            currentFilter = "all"
            applyFilters()
        }

        chipNew.setOnClickListener {
            currentFilter = "new"
            applyFilters()
        }

        chipUnassigned.setOnClickListener {
            currentFilter = "unassigned"
            applyFilters()
        }

        chipAssigned.setOnClickListener {
            currentFilter = "assigned"
            applyFilters()
        }

        chipMyCases.setOnClickListener {
            currentFilter = "my_cases"
            applyFilters()
        }

        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadCases()
        }

        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.police_blue)
        )
    }

    private fun setupRecyclerView() {
        adapter = CaseManagementAdapter(
            onCaseClick = { case ->
                val intent = Intent(this, CaseDetailsActivity::class.java)
                intent.putExtra("CASE_ID", case.id)
                startActivity(intent)
            },
            onAssignClick = { case ->
                assignCaseToMe(case)
            },
            currentUserId = currentUserId
        )
        rvCases.layoutManager = LinearLayoutManager(this)
        rvCases.adapter = adapter
    }

    private fun loadCases() {
        lifecycleScope.launch {
            swipeRefreshLayout.isRefreshing = true
            when (val result = caseRepository.getCases()) {
                is Resource.Success -> {
                    allCases = result.data ?: emptyList()
                    updateStats()
                    applyFilters()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@CaseManagementActivity,
                        result.message ?: "Failed to load cases",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun updateStats() {
        val unassignedCount = allCases.count { it.assignedTo == null && it.status?.lowercase() != "closed" }
        val inProgressCount = allCases.count {
            it.assignedTo != null &&
            (it.status?.lowercase() == "active" || it.status?.lowercase() == "assigned")
        }
        val myCasesCount = allCases.count { it.assignedTo == currentUserId }

        tvUnassignedCount.text = unassignedCount.toString()
        tvInProgressCount.text = inProgressCount.toString()
        tvMyCasesCount.text = myCasesCount.toString()
    }

    private fun applyFilters() {
        var filtered = allCases

        // Apply status filter
        filtered = when (currentFilter) {
            "new" -> filtered.filter {
                it.status?.lowercase() == "pending" || it.status?.lowercase() == "new"
            }
            "unassigned" -> filtered.filter {
                it.assignedTo == null && it.status?.lowercase() != "closed"
            }
            "assigned" -> filtered.filter {
                it.assignedTo != null && it.status?.lowercase() != "closed"
            }
            "my_cases" -> filtered.filter {
                it.assignedTo == currentUserId
            }
            else -> filtered // "all"
        }

        // Apply search filter
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { case ->
                case.id?.contains(searchQuery, ignoreCase = true) == true ||
                case.title?.contains(searchQuery, ignoreCase = true) == true ||
                case.category?.contains(searchQuery, ignoreCase = true) == true ||
                case.location?.contains(searchQuery, ignoreCase = true) == true ||
                case.description?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        filteredCases = filtered
        updateUI()
    }

    private fun updateUI() {
        adapter.submitList(filteredCases)

        tvResultsCount.text = getString(R.string.showing_results, filteredCases.size)

        if (filteredCases.isEmpty()) {
            rvCases.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvCases.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun assignCaseToMe(case: Case) {
        lifecycleScope.launch {
            when (val result = caseRepository.assignCase(case.id ?: "", currentUserId ?: "")) {
                is Resource.Success -> {
                    Toast.makeText(this@CaseManagementActivity, "Case assigned successfully", Toast.LENGTH_SHORT).show()
                    loadCases()
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@CaseManagementActivity,
                        result.message ?: "Failed to assign case",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }
}
