package com.rush47.tournament.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rush47.tournament.R
import com.rush47.tournament.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            val lang = if (binding.radioHindi.isChecked) "hi" else "en"

            val prefs = getSharedPreferences("rush47_prefs", MODE_PRIVATE)
            prefs.edit()
                .putBoolean("language_chosen", true)
                .putString("language", lang)
                .apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
