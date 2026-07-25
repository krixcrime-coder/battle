package com.rush47.tournament.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.rush47.tournament.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("rush47_prefs", MODE_PRIVATE)
        val languageChosen = prefs.getBoolean("language_chosen", false)
        val isLoggedIn = prefs.getString("auth_token", null) != null

        Handler(Looper.getMainLooper()).postDelayed({
            val next = when {
                !languageChosen -> LanguageActivity::class.java
                isLoggedIn -> HomeActivity::class.java
                else -> LoginActivity::class.java
            }
            startActivity(Intent(this, next))
            finish()
        }, 1500)
    }
}
