package com.dial112.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.dial112.R
import com.dial112.data.api.ApiClient
import com.dial112.data.local.TokenManager
import com.dial112.data.repository.AuthRepository
import com.dial112.databinding.ActivitySplashBinding
import com.dial112.util.Constants

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)
        authRepository = AuthRepository(ApiClient.apiService, tokenManager)

        // Start animations
        startLogoAnimation()
        startLoadingAnimation()

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DURATION)
    }

    private fun startLogoAnimation() {
        // Fade in logo
        binding.logoContainer.alpha = 0f
        binding.logoContainer.animate()
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Scale animation
        binding.logoContainer.scaleX = 0.8f
        binding.logoContainer.scaleY = 0.8f
        binding.logoContainer.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun startLoadingAnimation() {
        // Animate loading bar
        val animator = ObjectAnimator.ofFloat(
            binding.loadingBar,
            View.TRANSLATION_X,
            -200f,
            400f
        )
        animator.duration = 1500
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    private fun navigateToNextScreen() {
        val intent = if (authRepository.isLoggedIn()) {
            // User is logged in, navigate to appropriate home screen based on role
            val userRole = tokenManager.getUserRole()
            when (userRole) {
                Constants.ROLE_POLICE -> Intent(this, PoliceHomeActivity::class.java)
                Constants.ROLE_ADMIN -> Intent(this, AdminDashboardActivity::class.java)
                else -> Intent(this, CitizenHomeActivity::class.java)
            }
        } else {
            // User not logged in, navigate to login screen
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    companion object {
        private const val SPLASH_DURATION = 2500L
    }
}
