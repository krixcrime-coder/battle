package com.rush47.tournament.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rush47.tournament.api.ApiClient
import com.rush47.tournament.api.extractApiResponse
import com.rush47.tournament.databinding.ActivitySignupBinding
import com.rush47.tournament.models.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener { attemptSignup() }
        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val code = binding.etCode.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val referral = binding.etReferral.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
            mobile.isEmpty() || email.isEmpty() || password.isEmpty()
        ) {
            Toast.makeText(this, "Saari required fields bharo", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf(
            "first_name" to firstName,
            "last_name" to lastName,
            "username" to username,
            "country_code" to code,
            "mobile" to mobile,
            "email" to email,
            "password" to password,
            "referral_code" to referral
        )

        ApiClient.instance.register(body).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val result = extractApiResponse(response)
                Toast.makeText(this@SignupActivity, result?.message ?: "Signup failed", Toast.LENGTH_SHORT).show()
                if (result?.success == true) {
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
