package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SectionStudentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_section_students)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data from intent
        val programId = intent.getIntExtra("PROGRAM_ID", 0)
        val semester = intent.getStringExtra("SEMESTER") ?: ""
        val section = intent.getStringExtra("SECTION") ?: ""
        val programName = intent.getStringExtra("PROGRAM_NAME") ?: ""

        // Set title
        val titleTextView = findViewById<TextView>(R.id.sectionTitleTextView)
        titleTextView.text = "SECTION->  $semester$section ($programName)"

        // Fetch students
        fetchStudents(programId, semester, section)
    }

    private fun fetchStudents(programId: Int, semester: String, section: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetStudentsByProgramSemesterSection(
                    p_id = programId,
                    sem = semester,
                    section = section
                )

                if (response.isSuccessful) {
                    val studentsResponse = response.body()
                    if (studentsResponse?.success == true) {
                        val students = studentsResponse.students.map {
                            "${it.StudentId} - ${it.StudentName}"
                        }

                        withContext(Dispatchers.Main) {
                            setupListView(students)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@SectionStudentsActivity,
                                studentsResponse?.message ?: "Failed to fetch students",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@SectionStudentsActivity,
                            "Failed to fetch students",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SectionStudentsActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupListView(items: List<String>) {
        val listView = findViewById<ListView>(R.id.studentsListView)
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            items
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(this@SectionStudentsActivity, android.R.color.black))
                textView.gravity = Gravity.CENTER
                view.setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
                return view
            }
        }
        listView.adapter = adapter

        // Add click listener
        listView.setOnItemClickListener { parent, view, position, id ->
            // Get the selected student string (e.g., "12345 - John Doe")
            val selectedStudent = items[position]

            // Split the string to get ID and name
            val parts = selectedStudent.split(" - ")
            if (parts.size == 2) {
                val studentId = parts[0]
                val studentName = parts[1]

                // Start STUDENT_DASHBOARD activity
                val intent = Intent(this@SectionStudentsActivity, STUDENT_DASHBOARD::class.java).apply {
                    putExtra("UserID", studentId)
                    putExtra("UserName", studentName)
                }
                startActivity(intent)
            }
        }
    }

    // Extension function to convert dp to pixels
    fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}