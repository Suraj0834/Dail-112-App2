package com.dial112.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AIRepository
import com.dial112.util.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var ivBack: ImageView
    private lateinit var fabSearch: FrameLayout
    private lateinit var fabMyLocation: FrameLayout
    private lateinit var btnDirections: Button
    private lateinit var btnCall: Button

    private lateinit var aiRepository: AIRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_location)

        tokenManager = TokenManager(this)
        aiRepository = AIRepository(ApiClient.apiService, tokenManager)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initializeViews()
        setupListeners()
        initializeMap()
    }

    private fun initializeViews() {
        ivBack = findViewById(R.id.ivBack)
        fabSearch = findViewById(R.id.fabSearch)
        fabMyLocation = findViewById(R.id.fabMyLocation)
        btnDirections = findViewById(R.id.btnDirections)
        btnCall = findViewById(R.id.btnCall)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }
        fabSearch.setOnClickListener { openSearch() }
        fabMyLocation.setOnClickListener { getCurrentLocation() }
        btnDirections.setOnClickListener { openDirections() }
        btnCall.setOnClickListener { callEmergency() }
    }

    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable location if permission granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }

        // Load crime hotspots
        loadCrimeHotspots()
    }

    private fun openSearch() {
        Toast.makeText(this, "Search functionality coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            } else {
                // Default to Delhi if location not available
                val defaultLocation = LatLng(28.6139, 77.2090)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
            }
        }
    }

    private fun loadCrimeHotspots() {
        lifecycleScope.launch {
            when (val result = aiRepository.getCrimeHotspots()) {
                is Resource.Success -> {
                    val hotspots = result.data ?: emptyList()

                    hotspots.forEach { hotspot ->
                        val position = LatLng(hotspot.latitude, hotspot.longitude)

                        // Add circle overlay based on risk score
                        val color = when {
                            hotspot.riskScore >= 8.0 -> Color.argb(80, 220, 38, 38) // High risk - red
                            hotspot.riskScore >= 5.0 -> Color.argb(80, 251, 146, 60) // Medium risk - orange
                            else -> Color.argb(80, 250, 204, 21) // Low risk - yellow
                        }

                        googleMap?.addCircle(
                            CircleOptions()
                                .center(position)
                                .radius(500.0) // 500 meters radius
                                .fillColor(color)
                                .strokeColor(Color.TRANSPARENT)
                                .strokeWidth(0f)
                        )

                        // Add marker
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(hotspot.area ?: "Crime Hotspot")
                                .snippet("Risk: ${hotspot.riskScore}/10 • Incidents: ${hotspot.crimeCount}")
                        )
                    }

                    if (hotspots.isNotEmpty()) {
                        Toast.makeText(
                            this@MapLocationActivity,
                            "${hotspots.size} crime hotspots loaded",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(
                        this@MapLocationActivity,
                        result.message ?: "Failed to load crime hotspots",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun openDirections() {
        // Open Google Maps with directions to selected location
        // For now, just show a toast
        Toast.makeText(this, "Opening directions…", Toast.LENGTH_SHORT).show()
    }

    private fun callEmergency() {
        // Call emergency number 112
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap?.isMyLocationEnabled = true
                    getCurrentLocation()
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1003
    }
}
