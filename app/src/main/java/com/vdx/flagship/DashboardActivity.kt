package com.vdx.flagship

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vdx.flagship.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val adapter = FlagsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // Initial Fetch
        fetchFlags()

        // Pull to refresh
        binding.swipeRefresh.setOnRefreshListener {
            fetchFlags()
        }
    }

    private fun setupRecyclerView() {
        binding.rvFlags.layoutManager = LinearLayoutManager(this)
        binding.rvFlags.adapter = adapter
    }

    private fun fetchFlags() {
        binding.swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            // SDK Call: Get the raw list directly
            val flags = Flagship.fetchFlags()

            if (flags.isEmpty()) {
                Toast.makeText(
                    this@DashboardActivity,
                    "No flags found (or error)",
                    Toast.LENGTH_SHORT
                ).show()
            }

            adapter.submitList(flags)
            binding.swipeRefresh.isRefreshing = false
        }
    }
}
