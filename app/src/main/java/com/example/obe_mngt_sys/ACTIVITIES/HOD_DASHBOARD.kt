package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.obe_mngt_sys.ADAPTERS.ProgramAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.AddProgResponce
import com.example.obe_mngt_sys.MODELS.DeleteProgResponse
import com.example.obe_mngt_sys.MODELS.PostProgram
import com.example.obe_mngt_sys.MODELS.ProgramsResponse
import com.example.obe_mngt_sys.MODELS.Program
import com.example.obe_mngt_sys.MODELS.UpdateProgResponse
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.ActivityHodDashboard2Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HOD_DASHBOARD : AppCompatActivity() {

    private lateinit var binding: ActivityHodDashboard2Binding
    private lateinit var hodId: String
    private lateinit var programsData: List<Program>
    private lateinit var adapter: ProgramAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHodDashboard2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        hodId = intent.getStringExtra("UserID") ?: ""
        val hodName = intent.getStringExtra("UserName") ?: "Unknown HOD"
        binding.textViewHODName.text = hodName

        programsData = emptyList()
        adapter = ProgramAdapter(this, programsData, hodId, hodName) {
            getHODPrograms(hodId) // Refresh callback
        }
        binding.programsListView.adapter = adapter

        // Fetch HOD's programs
        getHODPrograms(hodId)

        // Handle add program button click
        binding.addIcon.setOnClickListener {
            showAddProgramDialog()
        }

        // Back Icon Functionality
        binding.backIcon.setOnClickListener {
            val intent = Intent(this@HOD_DASHBOARD, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun getHODPrograms(hodId: String) {
        val apiService = RetrofitInstance.apiService

        apiService.getHODPrograms(hodId).enqueue(object : Callback<ProgramsResponse> {
            override fun onResponse(call: Call<ProgramsResponse>, response: Response<ProgramsResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.Programs.isNotEmpty()) {
                        programsData = data.Programs
                        adapter = ProgramAdapter(
                            this@HOD_DASHBOARD,
                            programsData,
                            hodId,
                            binding.textViewHODName.text.toString()
                        ) {
                            getHODPrograms(hodId)
                        }
                        binding.programsListView.adapter = adapter
                    } else {
                        programsData = emptyList()
                        adapter = ProgramAdapter(
                            this@HOD_DASHBOARD,
                            programsData,
                            hodId,
                            binding.textViewHODName.text.toString()
                        ) {
                            getHODPrograms(hodId)
                        }
                        binding.programsListView.adapter = adapter
                        Toast.makeText(this@HOD_DASHBOARD, "No programs found.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    programsData = emptyList()
                    adapter = ProgramAdapter(
                        this@HOD_DASHBOARD,
                        programsData,
                        hodId,
                        binding.textViewHODName.text.toString()
                    ) {
                        getHODPrograms(hodId)
                    }
                    binding.programsListView.adapter = adapter
                    Toast.makeText(this@HOD_DASHBOARD, "Error fetching programs.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProgramsResponse>, t: Throwable) {
                Toast.makeText(this@HOD_DASHBOARD, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddProgramDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_program, null)
        val editTextProgramDesc = dialogView.findViewById<EditText>(R.id.editTextProgramDesc)

        AlertDialog.Builder(this)
            .setTitle("Add Program")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val programDesc = editTextProgramDesc.text.toString().trim()
                if (programDesc.isNotEmpty()) {
                    addProgram(programDesc)
                } else {
                    Toast.makeText(this, "Please enter a program description", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addProgram(programDesc: String) {
        val apiService = RetrofitInstance.apiService
        val newProgram = PostProgram(pname = programDesc, H_ID = hodId)

        apiService.addProgram(newProgram).enqueue(object : Callback<AddProgResponce> {
            override fun onResponse(call: Call<AddProgResponce>, response: Response<AddProgResponce>) {
                if (response.isSuccessful) {
                    val addedProgram = response.body()
                    Toast.makeText(
                        this@HOD_DASHBOARD,
                        addedProgram?.message ?: "Program added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    getHODPrograms(hodId)
                } else {
                    Toast.makeText(this@HOD_DASHBOARD, "Failed to add program", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddProgResponce>, t: Throwable) {
                Toast.makeText(this@HOD_DASHBOARD, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}