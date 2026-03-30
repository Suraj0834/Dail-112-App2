package com.dial112.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.CaseRepository
import com.dial112.data.repository.SOSRepository
import com.dial112.util.Resource
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sosRepository: SOSRepository
    private lateinit var caseRepository: CaseRepository
    private lateinit var tokenManager: TokenManager

    // Views
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvLastUpdated: TextView
    private lateinit var tvActiveCasesCount: TextView
    private lateinit var tvSosTodayCount: TextView
    private lateinit var tvOfficersCount: TextView
    private lateinit var tvCriminalRecordsCount: TextView
    private lateinit var tvResolvedCount: TextView
    private lateinit var tvOnDutyCount: TextView
    private lateinit var tvOffDutyCount: TextView
    private lateinit var tvDeploymentPercentage: TextView
    private lateinit var progressDeployment: ProgressBar

    // Buttons
    private lateinit var btnRefresh: android.widget.ImageButton
    private lateinit var btnFilter: android.widget.ImageButton
    private lateinit var btnDownload: android.widget.ImageButton

    // Charts
    private lateinit var chartMonthlyTrends: LineChart
    private lateinit var chartCrimeDistribution: PieChart
    private lateinit var chartResolutionRate: RadarChart

    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            updateCurrentTime()
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    private val dataRefreshRunnable = object : Runnable {
        override fun run() {
            loadDashboardData()
            handler.postDelayed(this, 30000) // Refresh every 30 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tokenManager = TokenManager(this)
        sosRepository = SOSRepository(ApiClient.apiService, tokenManager)
        caseRepository = CaseRepository(ApiClient.apiService, tokenManager)

        initializeViews()
        setupCharts()
        loadDashboardData()

        // Start time updates
        handler.post(timeUpdateRunnable)

        // Start auto-refresh
        handler.postDelayed(dataRefreshRunnable, 30000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeUpdateRunnable)
        handler.removeCallbacks(dataRefreshRunnable)
    }

    private fun initializeViews() {
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvLastUpdated = findViewById(R.id.tv_last_updated)
        tvActiveCasesCount = findViewById(R.id.tv_active_cases_count)
        tvSosTodayCount = findViewById(R.id.tv_sos_today_count)
        tvOfficersCount = findViewById(R.id.tv_officers_count)
        tvCriminalRecordsCount = findViewById(R.id.tv_criminal_records_count)
        tvResolvedCount = findViewById(R.id.tv_resolved_count)
        tvOnDutyCount = findViewById(R.id.tv_on_duty_count)
        tvOffDutyCount = findViewById(R.id.tv_off_duty_count)
        tvDeploymentPercentage = findViewById(R.id.tv_deployment_percentage)
        progressDeployment = findViewById(R.id.progress_deployment)

        btnRefresh = findViewById(R.id.btn_refresh)
        btnFilter = findViewById(R.id.btn_filter)
        btnDownload = findViewById(R.id.btn_download)

        chartMonthlyTrends = findViewById(R.id.chart_monthly_trends)
        chartCrimeDistribution = findViewById(R.id.chart_crime_distribution)
        chartResolutionRate = findViewById(R.id.chart_resolution_rate)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        btnRefresh.setOnClickListener {
            loadDashboardData()
            showMessage("Refreshing dashboard data...")
        }

        btnFilter.setOnClickListener {
            showMessage("Filter options coming soon")
        }

        btnDownload.setOnClickListener {
            showMessage("Export report coming soon")
        }
    }

    private fun updateCurrentTime() {
        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        tvCurrentTime.text = currentTime

        val lastUpdate = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date())
        tvLastUpdated.text = "Last updated: $lastUpdate"
    }

    private fun setupCharts() {
        setupLineChart()
        setupPieChart()
        setupRadarChart()
    }

    private fun setupLineChart() {
        chartMonthlyTrends.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)

            // X Axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#9E9E9E")
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
            }

            // Left Y Axis
            axisLeft.apply {
                textColor = Color.parseColor("#9E9E9E")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#2C2C2C")
                axisMinimum = 0f
            }

            // Right Y Axis
            axisRight.isEnabled = false

            // Legend
            legend.apply {
                textColor = Color.parseColor("#E0E0E0")
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }
        }

        // Sample data for monthly trends
        val newCasesEntries = ArrayList<Entry>()
        val resolvedCasesEntries = ArrayList<Entry>()

        newCasesEntries.add(Entry(0f, 45f))
        newCasesEntries.add(Entry(1f, 52f))
        newCasesEntries.add(Entry(2f, 48f))
        newCasesEntries.add(Entry(3f, 65f))
        newCasesEntries.add(Entry(4f, 58f))
        newCasesEntries.add(Entry(5f, 72f))

        resolvedCasesEntries.add(Entry(0f, 38f))
        resolvedCasesEntries.add(Entry(1f, 45f))
        resolvedCasesEntries.add(Entry(2f, 42f))
        resolvedCasesEntries.add(Entry(3f, 58f))
        resolvedCasesEntries.add(Entry(4f, 52f))
        resolvedCasesEntries.add(Entry(5f, 68f))

        val newCasesDataSet = LineDataSet(newCasesEntries, "New Cases").apply {
            color = Color.parseColor("#DC2626")
            setCircleColor(Color.parseColor("#DC2626"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = Color.parseColor("#E0E0E0")
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#DC2626")
            fillAlpha = 30
        }

        val resolvedCasesDataSet = LineDataSet(resolvedCasesEntries, "Resolved").apply {
            color = Color.parseColor("#10B981")
            setCircleColor(Color.parseColor("#10B981"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextColor = Color.parseColor("#E0E0E0")
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#10B981")
            fillAlpha = 30
        }

        val lineData = LineData(newCasesDataSet, resolvedCasesDataSet)
        chartMonthlyTrends.data = lineData
        chartMonthlyTrends.animateX(1000)
        chartMonthlyTrends.invalidate()
    }

    private fun setupPieChart() {
        chartCrimeDistribution.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawHoleEnabled(true)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(50)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = "Crime\nTypes"
            setCenterTextColor(Color.parseColor("#E0E0E0"))
            setCenterTextSize(14f)
            rotationAngle = 0f
            isRotationEnabled = false
            setEntryLabelColor(Color.parseColor("#E0E0E0"))
            setEntryLabelTextSize(10f)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = Color.parseColor("#E0E0E0")
                textSize = 11f
            }
        }

        // Sample crime distribution data
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(28f, "Theft"))
        entries.add(PieEntry(22f, "Assault"))
        entries.add(PieEntry(18f, "Robbery"))
        entries.add(PieEntry(15f, "Burglary"))
        entries.add(PieEntry(10f, "Fraud"))
        entries.add(PieEntry(7f, "Other"))

        val dataSet = PieDataSet(entries, "").apply {
            sliceSpace = 3f
            selectionShift = 5f
            colors = listOf(
                Color.parseColor("#DC2626"),
                Color.parseColor("#F97316"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#10B981"),
                Color.parseColor("#06B6D4"),
                Color.parseColor("#9333EA")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            valueFormatter = PercentFormatter(chartCrimeDistribution)
        }

        val data = PieData(dataSet)
        chartCrimeDistribution.data = data
        chartCrimeDistribution.animateY(1000)
        chartCrimeDistribution.invalidate()
    }

    private fun setupRadarChart() {
        chartResolutionRate.apply {
            description.isEnabled = false
            webLineWidth = 1f
            webColor = Color.parseColor("#2C2C2C")
            webLineWidthInner = 1f
            webColorInner = Color.parseColor("#2C2C2C")
            webAlpha = 100
            setBackgroundColor(Color.TRANSPARENT)

            xAxis.apply {
                textColor = Color.parseColor("#9E9E9E")
                textSize = 11f
                valueFormatter = IndexAxisValueFormatter(arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            }

            yAxis.apply {
                textColor = Color.parseColor("#9E9E9E")
                setLabelCount(5, false)
                axisMinimum = 0f
                axisMaximum = 100f
            }

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textColor = Color.parseColor("#E0E0E0")
            }
        }

        // Sample resolution rate data
        val entries = ArrayList<RadarEntry>()
        entries.add(RadarEntry(85f))
        entries.add(RadarEntry(78f))
        entries.add(RadarEntry(92f))
        entries.add(RadarEntry(88f))
        entries.add(RadarEntry(90f))
        entries.add(RadarEntry(75f))
        entries.add(RadarEntry(82f))

        val dataSet = RadarDataSet(entries, "Resolution Rate %").apply {
            color = Color.parseColor("#10B981")
            fillColor = Color.parseColor("#10B981")
            setDrawFilled(true)
            fillAlpha = 100
            lineWidth = 2f
            isDrawHighlightCircleEnabled = true
            setDrawHighlightIndicators(false)
            valueTextColor = Color.parseColor("#E0E0E0")
            valueTextSize = 10f
        }

        val data = RadarData(dataSet)
        chartResolutionRate.data = data
        chartResolutionRate.animateXY(1000, 1000)
        chartResolutionRate.invalidate()
    }

    private fun loadDashboardData() {
        // Load active cases
        lifecycleScope.launch {
            when (val result = caseRepository.getCases()) {
                is Resource.Success -> {
                    val cases = result.data ?: emptyList()
                    val activeCases = cases.filter {
                        it.status?.lowercase() != "closed" &&
                        it.status?.lowercase() != "resolved"
                    }
                    val resolvedCases = cases.filter { it.status?.lowercase() == "resolved" }

                    tvActiveCasesCount.text = activeCases.size.toString()
                    tvResolvedCount.text = resolvedCases.size.toString()
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load cases")
                }
                else -> {}
            }
        }

        // Load SOS alerts
        lifecycleScope.launch {
            when (val result = sosRepository.getActiveSOSAlerts()) {
                is Resource.Success -> {
                    val sosAlerts = result.data ?: emptyList()
                    tvSosTodayCount.text = sosAlerts.size.toString()
                }
                is Resource.Error -> {
                    showMessage(result.message ?: "Failed to load SOS alerts")
                }
                else -> {}
            }
        }

        // Simulate officer data (replace with real API when available)
        tvOfficersCount.text = "284"
        tvOnDutyCount.text = "284"
        tvOffDutyCount.text = "52"
        val deploymentRate = (284 * 100) / (284 + 52)
        tvDeploymentPercentage.text = "$deploymentRate%"
        progressDeployment.progress = deploymentRate

        // Simulate criminal records data
        tvCriminalRecordsCount.text = "1,247"
    }

    private fun showMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}
