package com.rush47.tournament.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rush47.tournament.R
import com.rush47.tournament.api.ApiClient
import com.rush47.tournament.api.extractApiResponse
import com.rush47.tournament.databinding.ActivityLoginBinding
import com.rush47.tournament.models.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
        binding.tvGoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username aur Password bharo", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf("username" to username, "password" to password)
        ApiClient.instance.login(body).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val result = extractApiResponse(response)
                if (result?.success == true) {
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                    // TODO: token aur user data ko SharedPreferences me save karo yahan se
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, result?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showForgotPasswordDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_forgot_password)
        dialog.setCancelable(true)

        // BUG FIX: Dialog by default sizes itself to wrap_content width, which was
        // squeezing the Cancel/Send OTP buttons and truncating their text ("CAN CEL").
        // Explicitly set the dialog window to match the screen width (with side margins)
        // so it renders exactly like the reference design.
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.9).toInt()
            window.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val etIdentifier = dialog.findViewById<EditText>(R.id.etIdentifier)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSendOtp = dialog.findViewById<Button>(R.id.btnSendOtp)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSendOtp.setOnClickListener {
            val identifier = etIdentifier.text.toString().trim()
            if (identifier.isEmpty()) {
                Toast.makeText(this, "Email ya Mobile No. daalo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val body = mapOf("identifier" to identifier)
            ApiClient.instance.forgotPassword(body).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    val result = extractApiResponse(response)
                    Toast.makeText(this@LoginActivity, result?.message ?: "OTP request failed", Toast.LENGTH_SHORT).show()
                    if (result?.success == true) dialog.dismiss()
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        dialog.show()
    }
}
