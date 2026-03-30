package com.dial112.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AIRepository
import com.dial112.databinding.ActivityCameraScannerBinding
import com.dial112.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var aiRepository: AIRepository
    private lateinit var tokenManager: TokenManager

    private var imageCapture: ImageCapture? = null
    private var scannerMode = PoliceHomeActivity.ScannerMode.FACE
    private var isFlashOn = false
    private var isUsingFrontCamera = false
    private var scanLineAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get scanner mode from intent
        intent.getStringExtra(PoliceHomeActivity.EXTRA_SCANNER_MODE)?.let {
            scannerMode = PoliceHomeActivity.ScannerMode.valueOf(it)
        }

        tokenManager = TokenManager(this)
        aiRepository = AIRepository(ApiClient.apiService, tokenManager)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        setupListeners()
        checkCameraPermission()
        startScanLineAnimation()
    }

    private fun setupUI() {
        // Update title based on mode
        binding.tvTitle.text = when (scannerMode) {
            PoliceHomeActivity.ScannerMode.FACE -> getString(R.string.camera_face_recognition)
            PoliceHomeActivity.ScannerMode.ANPR -> getString(R.string.camera_anpr)
            PoliceHomeActivity.ScannerMode.WEAPON -> getString(R.string.camera_weapon)
        }

        // Update instructions
        binding.tvInstructions.text = when (scannerMode) {
            PoliceHomeActivity.ScannerMode.FACE -> "Position face within the frame"
            PoliceHomeActivity.ScannerMode.ANPR -> "Point at license plate"
            PoliceHomeActivity.ScannerMode.WEAPON -> "Scan area for weapons"
        }

        // Set default tab
        binding.tabMode.selectTab(binding.tabMode.getTabAt(scannerMode.ordinal))
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Flash toggle
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }

        // Capture button
        binding.btnCapture.setOnClickListener {
            captureImage()
        }

        // Gallery button
        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        // Switch camera
        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }

        // Tab selection
        binding.tabMode.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                tab?.let {
                    scannerMode = PoliceHomeActivity.ScannerMode.values()[it.position]
                    setupUI()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        // View details button
        binding.btnViewDetails.setOnClickListener {
            // Navigate to details screen
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = if (isUsingFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                showMessage("Failed to start camera")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startScanLineAnimation() {
        scanLineAnimator = ObjectAnimator.ofFloat(
            binding.scanLine,
            View.TRANSLATION_Y,
            0f,
            binding.scannerFrame.height.toFloat() - binding.scanLine.height
        ).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        binding.btnFlash.setImageResource(
            if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
        )
        // Enable/disable flash on camera
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        // Show scanning state
        binding.tvStatus.text = getString(R.string.camera_scanning)
        binding.progressScanning.visibility = View.VISIBLE
        binding.cardResult.visibility = View.GONE

        // Create output file
        val photoFile = File(
            externalMediaDirs.firstOrNull(),
            "scan_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    processCapturedImage(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.progressScanning.visibility = View.GONE
                    showMessage("Failed to capture image: ${exception.message}")
                }
            }
        )
    }

    private fun processCapturedImage(imageFile: File) {
        lifecycleScope.launch {
            when (scannerMode) {
                PoliceHomeActivity.ScannerMode.FACE -> {
                    when (val result = aiRepository.recognizeFace(imageFile)) {
                        is Resource.Success -> {
                            val response = result.data!!
                            binding.progressScanning.visibility = View.GONE
                            binding.cardResult.visibility = View.VISIBLE

                            if (response.match && response.criminal != null) {
                                binding.tvResultTitle.text = getString(R.string.camera_match_found)
                                binding.tvResultDetails.text = buildString {
                                    append(response.criminal.name)
                                    append("\nDanger: ${response.criminal.dangerLevel}")
                                    append("\nConfidence: ${(response.confidence * 100).toInt()}%")
                                }
                                binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_error)
                                binding.ivResultIcon.setImageResource(R.drawable.ic_shield_alert)
                            } else {
                                binding.tvResultTitle.text = getString(R.string.camera_no_match)
                                binding.tvResultDetails.text = "No criminal match found"
                                binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_success)
                                binding.ivResultIcon.setImageResource(R.drawable.ic_check)
                            }
                            binding.tvStatus.text = "Scan Complete"
                        }
                        is Resource.Error -> {
                            binding.progressScanning.visibility = View.GONE
                            showMessage(result.message ?: "Face recognition failed")
                        }
                        else -> {}
                    }
                }

                PoliceHomeActivity.ScannerMode.ANPR -> {
                    when (val result = aiRepository.recognizeNumberPlate(imageFile)) {
                        is Resource.Success -> {
                            val response = result.data!!
                            binding.progressScanning.visibility = View.GONE
                            binding.cardResult.visibility = View.VISIBLE

                            if (response.plateNumber != null) {
                                binding.tvResultTitle.text = "Plate Identified"
                                binding.tvResultDetails.text = buildString {
                                    append("Plate: ${response.plateNumber}")
                                    if (response.vehicle != null) {
                                        append("\nOwner: ${response.vehicle.ownerName ?: "Unknown"}")
                                        append("\nVehicle: ${response.vehicle.model ?: "Unknown"}")
                                        if (response.vehicle.isStolenFlagged) {
                                            append("\n⚠️ STOLEN VEHICLE")
                                        }
                                    } else {
                                        append("\nNot registered")
                                    }
                                    append("\nConfidence: ${(response.confidence * 100).toInt()}%")
                                }
                                if (response.vehicle?.isStolenFlagged == true) {
                                    binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_error)
                                    binding.ivResultIcon.setImageResource(R.drawable.ic_shield_alert)
                                } else {
                                    binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_primary)
                                    binding.ivResultIcon.setImageResource(R.drawable.ic_car)
                                }
                            } else {
                                binding.tvResultTitle.text = getString(R.string.camera_no_match)
                                binding.tvResultDetails.text = "Could not read plate number"
                                binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_secondary)
                                binding.ivResultIcon.setImageResource(R.drawable.ic_car)
                            }
                            binding.tvStatus.text = "Scan Complete"
                        }
                        is Resource.Error -> {
                            binding.progressScanning.visibility = View.GONE
                            showMessage(result.message ?: "ANPR recognition failed")
                        }
                        else -> {}
                    }
                }

                PoliceHomeActivity.ScannerMode.WEAPON -> {
                    when (val result = aiRepository.detectWeapon(imageFile)) {
                        is Resource.Success -> {
                            val response = result.data!!
                            binding.progressScanning.visibility = View.GONE
                            binding.cardResult.visibility = View.VISIBLE

                            if (response.weaponDetected && response.detections.isNotEmpty()) {
                                binding.tvResultTitle.text = "⚠️ WEAPON DETECTED"
                                binding.tvResultDetails.text = buildString {
                                    append("Detected weapons:\n")
                                    response.detections.forEach { detection ->
                                        append("• ${detection.label.uppercase()} (${(detection.confidence * 100).toInt()}%)\n")
                                    }
                                }
                                binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_error)
                                binding.ivResultIcon.setImageResource(R.drawable.ic_shield_alert)
                            } else {
                                binding.tvResultTitle.text = "No Weapon Detected"
                                binding.tvResultDetails.text = "Area is safe"
                                binding.resultIconContainer.setBackgroundResource(R.drawable.bg_icon_container_success)
                                binding.ivResultIcon.setImageResource(R.drawable.ic_check)
                            }
                            binding.tvStatus.text = "Scan Complete"
                        }
                        is Resource.Error -> {
                            binding.progressScanning.visibility = View.GONE
                            showMessage(result.message ?: "Weapon detection failed")
                        }
                        else -> {}
                    }
                }
            }

            // Clean up temp file
            imageFile.delete()
        }
    }

    private fun openGallery() {
        // Open gallery to select image
    }

    private fun switchCamera() {
        isUsingFrontCamera = !isUsingFrontCamera
        startCamera()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                showMessage("Camera permission required")
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        scanLineAnimator?.cancel()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1002
    }
}
