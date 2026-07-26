package com.rush47.tournament.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rush47.tournament.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("rush47_prefs", MODE_PRIVATE)
        val firstName = prefs.getString("first_name", null)

        binding.tvWelcome.text = if (!firstName.isNullOrEmpty()) {
            "Welcome, $firstName! 👋"
        } else {
            "Welcome to RUSH 47! 👋"
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
