package com.vdx.flagship

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vdx.flagship.databinding.ActivityLoginBinding
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "flagship_demo_prefs"
        private const val KEY_API_KEY = "saved_api_key"
        private const val KEY_BASE_URL = "saved_base_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Auto-fill saved data
        val savedApiKey = prefs.getString(KEY_API_KEY, "")
        val savedBaseUrl = prefs.getString(KEY_BASE_URL, "")

        if (!savedApiKey.isNullOrEmpty()) {
            binding.etApiKey.setText(savedApiKey)
        }
        if (!savedBaseUrl.isNullOrEmpty()) {
            binding.etBaseUrl.setText(savedBaseUrl)
        }

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.btnClear.setOnClickListener {
            clearCredentials()
        }
    }

    private fun handleLogin() {
        val apiKey = binding.etApiKey.text.toString().trim()
        val baseUrl = binding.etBaseUrl.text.toString().trim()

        if (apiKey.isEmpty()) {
            binding.etApiKey.error = "API Key is required"
            return
        }

        if (baseUrl.isEmpty()) {
            binding.etBaseUrl.error = "Base URL is required"
            return
        }

        if (!URLUtil.isValidUrl(baseUrl)) {
            binding.etBaseUrl.error = "Please enter a valid URL (e.g., https://...)"
            return
        }

        try {
            // Initialize SDK
            Flagship.initialize(
                context = applicationContext,
                apiKey = apiKey,
                baseUrl = baseUrl,
                loadData = false
            )

            // Save credentials on success
            saveCredentials(apiKey, baseUrl)

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Init failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCredentials(apiKey: String, baseUrl: String) {
        prefs.edit().apply {
            putString(KEY_API_KEY, apiKey)
            putString(KEY_BASE_URL, baseUrl)
            apply()
        }
    }

    private fun clearCredentials() {
        // Clear inputs
        binding.etApiKey.text?.clear()
        binding.etBaseUrl.text?.clear()

        // Clear saved data
        prefs.edit { clear() }

        Toast.makeText(this, "Credentials cleared", Toast.LENGTH_SHORT).show()
    }
}
