package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.ProgramInfo
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Advisor_section : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_advisor_section)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get teacherId and teacherName from intent
        val teacherId = intent.getStringExtra("UserID") ?: ""
        val teacherName = intent.getStringExtra("UserName") ?: ""

        // Set advisor name
        val advisorNameTextView = findViewById<TextView>(R.id.advisorNameTextView)
        advisorNameTextView.text = "Advisor: $teacherName"

        // Setup toggle switch
        val roleSwitch = findViewById<Switch>(R.id.roleSwitch)
        roleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to Teacher Dashboard
                val intent = Intent(this@Advisor_section, TEACHER_DASHBOARD::class.java)
                intent.putExtra("UserID", teacherId)
                intent.putExtra("UserName", teacherName)
                startActivity(intent)
                finish()
            }
        }

        // Fetch and display advisor sections
        fetchAdvisorInfo(teacherId)
    }

    private fun fetchAdvisorInfo(teacherId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetAdvisorInfo(teacherId)
                if (response.isSuccessful) {
                    val advisorResponse = response.body()
                    if (advisorResponse?.success == true) {
                        val programs = advisorResponse.data.Programs.map { program ->
                            ProgramInfo(
                                ProgramId = program.ProgramId,
                                ProgramName = program.ProgramName,
                                Section = program.Section,
                                Semester = program.Semester
                            )
                        }
                        val items = programs.map { program ->
                            "${program.Semester}${program.Section} (${program.ProgramName})"
                        }

                        withContext(Dispatchers.Main) {
                            setupListView(items, programs)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@Advisor_section,
                            "Failed to fetch advisor information",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Advisor_section,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupListView(items: List<String>, programs: List<ProgramInfo>) {
        val listView = findViewById<ListView>(R.id.advisorListView)
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(this@Advisor_section, android.R.color.black))
                textView.gravity = Gravity.CENTER
                return view
            }
        }
        listView.adapter = adapter

        // Add item click listener
        listView.setOnItemClickListener { _, _, position, _ ->
            val program = programs[position]
            val intent = Intent(this@Advisor_section, SectionStudentsActivity::class.java)
            intent.putExtra("PROGRAM_ID", program.ProgramId)
            intent.putExtra("SEMESTER", program.Semester.toString())
            intent.putExtra("SECTION", program.Section)
            intent.putExtra("PROGRAM_NAME", program.ProgramName)
            startActivity(intent)
        }
    }
}