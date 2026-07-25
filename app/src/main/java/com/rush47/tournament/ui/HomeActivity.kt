package com.rush47.tournament.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout

// Placeholder Home screen — yahan se aage tournaments list, wallet, profile
// wagera screens banane hain jab tu next batayega.
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 200, 48, 48)
        val tv = TextView(this)
        tv.text = "Welcome to RUSH 47 🎮\n(Tournament list yahan aayegi)"
        tv.textSize = 20f
        layout.addView(tv)
        setContentView(layout)
    }
}
