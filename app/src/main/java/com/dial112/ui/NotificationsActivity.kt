package com.dial112.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dial112.R
import com.google.android.material.chip.Chip

class NotificationsActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var tvClearAll: TextView
    private lateinit var tvTotalCount: TextView
    private lateinit var tvUnreadCount: TextView
    private lateinit var chipAll: Chip
    private lateinit var chipUnread: Chip
    private lateinit var chipCases: Chip
    private lateinit var chipAlerts: Chip
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvNotifications: RecyclerView
    private lateinit var llEmptyState: LinearLayout

    private var allNotifications = listOf<Notification>()
    private var filteredNotifications = listOf<Notification>()
    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        initializeViews()
        setupListeners()
        setupRecyclerView()
        loadNotifications()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        tvClearAll = findViewById(R.id.tvClearAll)
        tvTotalCount = findViewById(R.id.tvTotalCount)
        tvUnreadCount = findViewById(R.id.tvUnreadCount)
        chipAll = findViewById(R.id.chipAll)
        chipUnread = findViewById(R.id.chipUnread)
        chipCases = findViewById(R.id.chipCases)
        chipAlerts = findViewById(R.id.chipAlerts)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        rvNotifications = findViewById(R.id.rvNotifications)
        llEmptyState = findViewById(R.id.llEmptyState)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }
        tvClearAll.setOnClickListener { clearAllNotifications() }

        swipeRefresh.setOnRefreshListener {
            loadNotifications()
        }

        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "all"
                applyFilter()
            }
        }

        chipUnread.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "unread"
                applyFilter()
            }
        }

        chipCases.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "cases"
                applyFilter()
            }
        }

        chipAlerts.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = "alerts"
                applyFilter()
            }
        }
    }

    private fun setupRecyclerView() {
        rvNotifications.layoutManager = LinearLayoutManager(this)
        // Adapter will be set in loadNotifications()
    }

    private fun loadNotifications() {
        swipeRefresh.isRefreshing = true

        // TODO: Fetch notifications from backend/database
        // For now, using mock data
        allNotifications = getMockNotifications()

        applyFilter()
        updateStats()

        swipeRefresh.isRefreshing = false
    }

    private fun applyFilter() {
        filteredNotifications = when (currentFilter) {
            "unread" -> allNotifications.filter { !it.isRead }
            "cases" -> allNotifications.filter { it.category == "Case Update" || it.category == "Case Assigned" }
            "alerts" -> allNotifications.filter { it.category == "Alert" || it.category == "Emergency" }
            else -> allNotifications
        }

        updateUI()
    }

    private fun updateUI() {
        if (filteredNotifications.isEmpty()) {
            llEmptyState.visibility = View.VISIBLE
            rvNotifications.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvNotifications.visibility = View.VISIBLE
            // TODO: Update adapter with filteredNotifications
        }
    }

    private fun updateStats() {
        val unreadCount = allNotifications.count { !it.isRead }
        tvTotalCount.text = allNotifications.size.toString()
        tvUnreadCount.text = unreadCount.toString()
    }

    private fun clearAllNotifications() {
        // TODO: Clear all notifications from backend/database
        allNotifications = emptyList()
        applyFilter()
        updateStats()
    }

    private fun getMockNotifications(): List<Notification> {
        // Mock data for testing
        return listOf(
            Notification("1", "Case Update", "Your case #C12345 has been assigned to Officer Sharma", "Case Update", false, System.currentTimeMillis() - 120000),
            Notification("2", "Alert", "Crime reported in your area - Connaught Place", "Alert", true, System.currentTimeMillis() - 300000),
            Notification("3", "Case Assigned", "New case assigned to you #C12346", "Case Assigned", false, System.currentTimeMillis() - 600000)
        )
    }

    data class Notification(
        val id: String,
        val title: String,
        val message: String,
        val category: String,
        val isRead: Boolean,
        val timestamp: Long
    )
}
