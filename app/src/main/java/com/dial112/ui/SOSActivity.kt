package com.dial112.ui

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.SOSRepository
import com.dial112.databinding.ActivitySosBinding
import com.dial112.service.SOSForegroundService
import com.dial112.util.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class SOSActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sosRepository: SOSRepository

    private var holdTimer: CountDownTimer? = null
    private var isHolding = false
    private var sosTriggered = false
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val tokenManager = TokenManager(this)
        sosRepository = SOSRepository(ApiClient.apiService, tokenManager)

        setupUI()
        setupListeners()
        getCurrentLocation()
        startPulseAnimation()
    }

    private fun setupUI() {
        // Initial state
        binding.tvHoldInstruction.text = "Hold 3s"
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // SOS Button - Long press to trigger
        binding.btnSOS.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startHoldTimer()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    cancelHoldTimer()
                    true
                }
                else -> false
            }
        }

        // Share location
        binding.btnShareLocation.setOnClickListener {
            shareLocation()
        }

        // Call police
        binding.btnCallPolice.setOnClickListener {
            callPolice()
        }
    }

    private fun startHoldTimer() {
        isHolding = true
        holdTimer = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000) + 1
                binding.tvHoldInstruction.text = "${secondsLeft}s"
                
                // Scale animation while holding
                val progress = 1 - (millisUntilFinished / 3000f)
                val scale = 1f + (progress * 0.1f)
                binding.btnSOS.scaleX = scale
                binding.btnSOS.scaleY = scale
            }

            override fun onFinish() {
                triggerSOS()
            }
        }.start()
    }

    private fun cancelHoldTimer() {
        isHolding = false
        holdTimer?.cancel()
        binding.tvHoldInstruction.text = "Hold 3s"
        
        // Reset scale
        binding.btnSOS.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()
    }

    private fun triggerSOS() {
        sosTriggered = true

        // Update UI
        binding.tvHoldInstruction.text = "SENDING..."

        // Vibrate
        val vibrator = getSystemService(VIBRATOR_SERVICE) as android.os.Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        }

        // Send SOS to server
        sendSOSToServer()

        // Start foreground service for location tracking
        startSOSForegroundService()
    }

    private fun sendSOSToServer() {
        val location = currentLocation
        if (location == null) {
            showMessage("Unable to get location. Please enable GPS.")
            binding.tvHoldInstruction.text = "Hold 3s"
            sosTriggered = false
            return
        }

        lifecycleScope.launch {
            when (val result = sosRepository.triggerSOS(
                latitude = location.latitude,
                longitude = location.longitude,
                address = binding.tvLocation.text.toString()
            )) {
                is Resource.Success -> {
                    binding.tvHoldInstruction.text = "SENT"
                    showMessage("SOS Alert Sent! Help is on the way.")
                }
                is Resource.Error -> {
                    binding.tvHoldInstruction.text = "FAILED"
                    showMessage(result.message ?: "Failed to send SOS")
                    sosTriggered = false
                }
                else -> {}
            }
        }
    }

    private fun startSOSForegroundService() {
        val intent = Intent(this, SOSForegroundService::class.java)
        intent.action = SOSForegroundService.ACTION_START_SOS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun startPulseAnimation() {
        // Inner ring pulse
        val scaleXInner = ObjectAnimator.ofFloat(binding.pulseRingInner, "scaleX", 1f, 1.15f, 1f)
        val scaleYInner = ObjectAnimator.ofFloat(binding.pulseRingInner, "scaleY", 1f, 1.15f, 1f)
        val alphaInner = ObjectAnimator.ofFloat(binding.pulseRingInner, "alpha", 0.6f, 0.3f, 0.6f)

        val innerAnimator = AnimatorSet().apply {
            playTogether(scaleXInner, scaleYInner, alphaInner)
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Outer ring pulse 1
        val scaleXOuter1 = ObjectAnimator.ofFloat(binding.pulseRingOuter1, "scaleX", 1f, 1.25f, 1f)
        val scaleYOuter1 = ObjectAnimator.ofFloat(binding.pulseRingOuter1, "scaleY", 1f, 1.25f, 1f)
        val alphaOuter1 = ObjectAnimator.ofFloat(binding.pulseRingOuter1, "alpha", 0.4f, 0.15f, 0.4f)

        val outerAnimator1 = AnimatorSet().apply {
            playTogether(scaleXOuter1, scaleYOuter1, alphaOuter1)
            duration = 2000
            startDelay = 300
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Outer ring pulse 2
        val scaleXOuter2 = ObjectAnimator.ofFloat(binding.pulseRingOuter2, "scaleX", 1f, 1.2f, 1f)
        val scaleYOuter2 = ObjectAnimator.ofFloat(binding.pulseRingOuter2, "scaleY", 1f, 1.2f, 1f)
        val alphaOuter2 = ObjectAnimator.ofFloat(binding.pulseRingOuter2, "alpha", 0.3f, 0.1f, 0.3f)

        val outerAnimator2 = AnimatorSet().apply {
            playTogether(scaleXOuter2, scaleYOuter2, alphaOuter2)
            duration = 2000
            startDelay = 600
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Combine and repeat
        innerAnimator.start()
        outerAnimator1.start()
        outerAnimator2.start()

        // Repeat animations
        binding.root.postDelayed({
            if (!isFinishing) {
                startPulseAnimation()
            }
        }, 2000)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                currentLocation = it
                updateLocationUI(it)
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    append(address.thoroughfare ?: "")
                    if (address.subLocality != null) append(", ${address.subLocality}")
                    if (address.locality != null) append(", ${address.locality}")
                }
                binding.tvLocation.text = addressText
            }
        } catch (e: Exception) {
            binding.tvLocation.text = "${location.latitude}, ${location.longitude}"
        }
    }

    private fun shareLocation() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I need help! My location: ${binding.tvLocation.text}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Location"))
    }

    private fun callPolice() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:112")
        }
        startActivity(intent)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        holdTimer?.cancel()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }
}
