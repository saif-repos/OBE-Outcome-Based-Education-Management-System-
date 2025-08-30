package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.obe_mngt_sys.ADAPTERS.MappingApprovalAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.TeacherMappingResponse
import com.example.obe_mngt_sys.databinding.ActivityMappingForApprovalsScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MAPPING_FOR_APPROVALS_SCREEN : AppCompatActivity() {

    private lateinit var binding: ActivityMappingForApprovalsScreenBinding
    private lateinit var adapter: MappingApprovalAdapter
    private var programId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMappingForApprovalsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize views and setup
        initViews()
        setupRecyclerView()

        // Fetch data if programId is valid
        if (programId != -1) {
            fetchMappingData(programId)
        } else {
            showError("Invalid program ID")
            finish()
        }
    }

    private fun initViews() {
        // Get passed data from intent
        programId = intent.getIntExtra("PROGRAM_ID", -1)
        val hodName = intent.getStringExtra("HOD_NAME") ?: ""

        // Set up toolbar
        binding.textViewHODName.text = hodName

        // Set up back button
        binding.backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = MappingApprovalAdapter(
            onViewClick = { mapping ->
                showMappingDetails(mapping)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MAPPING_FOR_APPROVALS_SCREEN)
            adapter = this@MAPPING_FOR_APPROVALS_SCREEN.adapter
            setHasFixedSize(true)
        }
    }

    private fun fetchMappingData(programId: Int) {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getTeacherMappingsByProgram(programId)

                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        response.body()?.let { mappings ->
                            if (mappings.isNotEmpty()) {
                                adapter.submitList(mappings)
                                binding.emptyStateView.visibility = View.GONE
                            } else {
                                showEmptyState()
                            }
                        } ?: showError("No data received from server")
                    } else {
                        showError("Server error: ${response.code()}")
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("HTTP error: ${e.message()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showError("Network error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showMappingDetails(mapping: TeacherMappingResponse) {
        val intent = Intent(this, View_Maping_HOD::class.java).apply {
            putExtra("offeredCourseId", mapping.offeredCourseId)
            putExtra("courseName", mapping.courseName)
            putExtra("teacherName", mapping.teacherName)
            putExtra("hodName", binding.textViewHODName.text.toString())
            putExtra("status", mapping.status)
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        binding.emptyStateView.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}