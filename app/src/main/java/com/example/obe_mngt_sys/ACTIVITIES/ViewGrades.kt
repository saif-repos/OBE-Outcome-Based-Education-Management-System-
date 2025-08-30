package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.CourseResultAdapter
import com.example.obe_mngt_sys.MODELS.StudentAcademicRecord
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewGrades : AppCompatActivity() {
    private lateinit var semesterResultsContainer: LinearLayout
    private lateinit var textViewCGPA: TextView
    private lateinit var textViewTotalCredits: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_grades)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        handleIntentData()
        setupBackButton()
        fetchStudentResults()
    }

    private fun initializeViews() {
        semesterResultsContainer = findViewById(R.id.semesterResultsContainer)
        textViewCGPA = findViewById(R.id.textViewCGPA)
        textViewTotalCredits = findViewById(R.id.textViewTotalCredits)
        findViewById<LinearLayout>(R.id.main).setBackgroundColor(Color.WHITE)
    }

    private fun handleIntentData() {
        intent?.let {
            val studentName = it.getStringExtra("UserName") ?: ""
            findViewById<TextView>(R.id.textViewStudentName).apply {
                text = studentName
                setTextColor(Color.BLACK)
                textSize = 20f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun setupBackButton() {
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack?.setOnClickListener {
            intent?.let {
                val studentId = it.getStringExtra("UserID") ?: ""
                val studentName = it.getStringExtra("UserName") ?: ""
                Intent(this, STUDENT_DASHBOARD::class.java).apply {
                    putExtra("UserID", studentId)
                    putExtra("UserName", studentName)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }.also { intent -> startActivity(intent) }
                finish()
            }
        } ?: run {
            // Log error or show toast if button is not found
            android.util.Log.e("ViewGrades", "Back button not found in layout")
        }
    }

    private fun fetchStudentResults() {
        val studentId = intent?.getStringExtra("UserID") ?: run {
            showError("Student ID not found")
            return
        }

        RetrofitInstance.apiService.getStudentResults(studentId)
            .enqueue(object : Callback<StudentAcademicRecord> {
                override fun onResponse(
                    call: Call<StudentAcademicRecord>,
                    response: Response<StudentAcademicRecord>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.let { updateUI(it) }
                    } else {
                        showError("Failed to load data")
                    }
                }

                override fun onFailure(call: Call<StudentAcademicRecord>, t: Throwable) {
                    showError("Network error: ${t.message}")
                }
            })
    }

    private fun updateUI(academicRecord: StudentAcademicRecord) {
        textViewCGPA.apply {
            text = "CGPA: ${String.format("%.2f", academicRecord.CGPA)}"
            setTextColor(Color.BLACK)
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        textViewTotalCredits.apply {
            text = "Total Credits: ${academicRecord.TotalCreditHoursCompleted}"
            setTextColor(Color.BLACK)
            textSize = 16f
        }

        semesterResultsContainer.removeAllViews()

        academicRecord.Semesters.forEach { semester ->
            val semesterView = LayoutInflater.from(this)
                .inflate(R.layout.item_semester, semesterResultsContainer, false).apply {
                    setBackgroundResource(R.drawable.semester_background)
                }

            semesterView.findViewById<TextView>(R.id.textViewSemesterName).apply {
                text = "Session: ${semester.sesionyear}"
                setTextColor(Color.parseColor("#333333"))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            semesterView.findViewById<TextView>(R.id.textViewSemesterGPA).apply {
                text = "GPA: ${String.format("%.2f", semester.GPA)}"
                setTextColor(Color.parseColor("#555555"))
                textSize = 16f
            }

            semesterView.findViewById<RecyclerView>(R.id.recyclerViewCourses).apply {
                layoutManager = LinearLayoutManager(this@ViewGrades)
                adapter = CourseResultAdapter(semester.Courses)
            }

            semesterResultsContainer.addView(semesterView)
        }
    }

    private fun showError(message: String) {
        textViewCGPA.text = message
        textViewCGPA.setTextColor(Color.RED)
    }
}