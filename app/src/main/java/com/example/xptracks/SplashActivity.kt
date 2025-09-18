package com.example.xptracks

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    private lateinit var splashIcon: ImageView
    private lateinit var splashAppName: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var originalText: String = ""
    private var isTransitioning = false

    private val totalSplashDuration = 8000L // 8 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle the splash screen transition.
        installSplashScreen()
        setContentView(R.layout.activity_splash)

        splashIcon = findViewById(R.id.splash_icon)
        splashAppName = findViewById(R.id.splash_app_name)
        originalText = getString(R.string.app_name) // Use string resource
        splashAppName.text = "" // Start with empty text

        startAnimations()
    }

    private fun startAnimations() {
        if (isFinishing) return
        splashIcon.alpha = 0f // Start fully transparent for fade-in

        // 1. Icon Fade-in
        val iconFadeIn = ObjectAnimator.ofFloat(splashIcon, View.ALPHA, 0f, 1f).apply {
            duration = 1000 // 1 second
            interpolator = AccelerateDecelerateInterpolator()
        }

        // 2. Icon Ripple (Scale animation)
        val iconScaleX = ObjectAnimator.ofFloat(splashIcon, View.SCALE_X, 1f, 1.2f, 1f).apply {
            duration = 800 // 0.8 seconds
            interpolator = AnticipateOvershootInterpolator()
        }
        val iconScaleY = ObjectAnimator.ofFloat(splashIcon, View.SCALE_Y, 1f, 1.2f, 1f).apply {
            duration = 800 // 0.8 seconds
            interpolator = AnticipateOvershootInterpolator()
        }
        val rippleAnimation = AnimatorSet().apply {
            playTogether(iconScaleX, iconScaleY)
        }

        // Sequence: Icon Fade-in -> Icon Ripple
        val iconIntroAnimation = AnimatorSet()
        iconIntroAnimation.playSequentially(iconFadeIn, rippleAnimation)
        iconIntroAnimation.start()

        // 3. App Name Text - Type In (after icon ripple)
        handler.postDelayed({
            if (!isFinishing && !isTransitioning) {
                animateText(true) // true for typing in
            }
        }, iconFadeIn.duration + rippleAnimation.duration + 200) // Start after icon animations + short delay
    }

    private fun animateText(isTypingIn: Boolean) {
        if (isFinishing || isTransitioning) return

        val textToAnimate = originalText
        val textLength = textToAnimate.length
        val animationSpeed = 150L // milliseconds per character

        if (isTypingIn) {
            splashAppName.text = ""
            var charIndex = 0
            val typingRunnable = object : Runnable {
                override fun run() {
                    if (isFinishing || isTransitioning) return
                    if (charIndex <= textLength) {
                        splashAppName.text = textToAnimate.substring(0, charIndex)
                        charIndex++
                        handler.postDelayed(this, animationSpeed)
                    } else {
                        // Typing finished, wait then start deleting
                        handler.postDelayed({
                            if (!isFinishing && !isTransitioning) {
                                animateText(false) // false for deleting
                            }
                        }, 1000) // Wait 1 second before deleting
                    }
                }
            }
            handler.post(typingRunnable)
        } else {
            // Deleting text
            var charIndex = textLength
            val deletingRunnable = object : Runnable {
                override fun run() {
                    if (isFinishing || isTransitioning) return
                    if (charIndex >= 0) {
                        splashAppName.text = textToAnimate.substring(0, charIndex)
                        charIndex--
                        handler.postDelayed(this, animationSpeed)
                    } else {
                        // Deleting finished, fade out icon and then transition
                        fadeOutIconAndFinish()
                    }
                }
            }
            handler.post(deletingRunnable)
        }
    }

    private fun fadeOutIconAndFinish() {
        if (isFinishing || isTransitioning) return

        // 4. Icon Fade-out
        val iconFadeOut = ObjectAnimator.ofFloat(splashIcon, View.ALPHA, 1f, 0f).apply {
            duration = 500 //
            interpolator = AccelerateDecelerateInterpolator()
        }
        iconFadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                navigateToNextScreen()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {
                navigateToNextScreen() // Also navigate if animation is cancelled
            }
        })
        iconFadeOut.start()
    }

    private fun navigateToNextScreen() {
        if (!isTransitioning) {
            isTransitioning = true
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Fallback to ensure splash screen transitions after totalSplashDuration
        // if animations haven't completed and triggered the transition.
        handler.postDelayed({
            if (!isFinishing && !isTransitioning) {
                navigateToNextScreen()
            }
        }, totalSplashDuration)
    }

    override fun onPause() {
        super.onPause()
        // Clean up animations and handlers if activity is paused.
        handler.removeCallbacksAndMessages(null)
        // If a transition has started, ensure activity finishes.
        if (isTransitioning && !isFinishing) {
             finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
