package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.MODELS.PloResultResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class PLOBASEDRESULT_OVERALL : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tableContainer: TableLayout
    private lateinit var tvStudentName: TextView
    private lateinit var btnViewDetailedResults: Button
    private lateinit var btnViewSemesterGrades: Button
    private lateinit var btnViewCloBasedResults: Button // Added this
    private lateinit var btnBack: ImageButton
    private lateinit var dificultylevel:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plobasedresult_overall)

        initViews()
        setupStudentInfo()
        setupButtonClickListeners()

        // Get student ID safely
        val studentId = intent.getStringExtra("UserID") ?: run {
            showError("Student ID not found")
            return
        }
        fetchPloResults(studentId)
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tableContainer = findViewById(R.id.tableContainer)
        tvStudentName = findViewById(R.id.textViewStudentName)
        btnViewDetailedResults = findViewById(R.id.btnViewDetailedResults)
        btnViewSemesterGrades = findViewById(R.id.btnViewSemesterGrades)
        btnViewCloBasedResults = findViewById(R.id.btnViewCloBasedResults) // Initialize this
        dificultylevel = findViewById(R.id.dificultylevel)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupButtonClickListeners() {
        btnViewDetailedResults.setOnClickListener {
            navigateToStudentDashboard(
                intent.getStringExtra("UserID") ?: "",
                intent.getStringExtra("UserName") ?: ""
            )
        }

        btnViewSemesterGrades.setOnClickListener {
            navigateToViewGrades(
                intent.getStringExtra("UserID") ?: "",
                intent.getStringExtra("UserName") ?: ""
            )
        }

        btnViewCloBasedResults.setOnClickListener {
            navigateToCloBasedAllPlos(
                intent.getStringExtra("UserID") ?: "",
                intent.getStringExtra("UserName") ?: ""
            )
        }

        dificultylevel.setOnClickListener {
            dlevel(
                intent.getStringExtra("UserID") ?: "",
                intent.getStringExtra("UserName") ?: ""
            )
        }


        btnBack.setOnClickListener { navigateToLogin() }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupStudentInfo() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        tvStudentName.text = studentName
    }

    private fun navigateToStudentDashboard(studentId: String, studentName: String) {
        startActivity(Intent(this, STUDENT_DASHBOARD::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
        })
    }

    private fun navigateToViewGrades(studentId: String, studentName: String) {
        startActivity(Intent(this, ViewGrades::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
        })
    }

    private fun navigateToCloBasedAllPlos(studentId: String, studentName: String) {
        startActivity(Intent(this, CLOBASEDALLPLOS::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
        })
    }
    private fun dlevel(studentId: String, studentName: String) {
        startActivity(Intent(this, Examples::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
        })
    }



    private fun fetchPloResults(studentId: String) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
                withContext(Dispatchers.Main) {
                    handlePloResponse(response)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    showError("HTTP error: ${e.message()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error: ${e.message}")
                }
            }
        }
    }

    private fun handlePloResponse(response: Response<List<PloResultResponse>>) {
        progressBar.visibility = View.GONE
        if (response.isSuccessful) {
            response.body()?.let { ploResults ->
                if (ploResults.isEmpty()) {
                    showError("No PLO results found")
                } else {
                    displayOverallPloResults(ploResults)
                }
            } ?: showError("Empty response received")
        } else {
            showError("Server error: ${response.code()}")
        }
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun displayOverallPloResults(ploResults: List<PloResultResponse>) {
        tableContainer.removeAllViews()

        // Add header row
        val headerRow = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        headerRow.addView(createHeaderCell("PLO", true))
        headerRow.addView(createHeaderCell("Score 100%", false))
        tableContainer.addView(headerRow)

        // Add PLO rows
        ploResults.sortedBy { it.PloId }.forEach { plo ->
            val row = TableRow(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.addView(createCell("PLO ${plo.PloId}", true))

            val score = Math.round(plo.FinalPloScore).toInt()
            val scoreCell = createCell("$score", false).apply {
                setTextColor(
                    if (score >= 70) ContextCompat.getColor(context, R.color.green)
                    else ContextCompat.getColor(context, R.color.red)
                )
            }
            row.addView(scoreCell)

            tableContainer.addView(row)
        }
    }

    private fun createHeaderCell(text: String, isFirstColumn: Boolean): TextView {
        return TextView(this).apply {
            setText(text)
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                if (isFirstColumn) 0.6f else 0.4f
            )

            background = ContextCompat.getDrawable(context, R.color.light_gray)
        }
    }

    private fun createCell(text: String, isFirstColumn: Boolean): TextView {
        return TextView(this).apply {
            setText(text)
            setTextAppearance(android.R.style.TextAppearance_Medium)
            gravity = Gravity.CENTER
            setPadding(16, 12, 16, 12)

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                if (isFirstColumn) 0.6f else 0.4f
            )

            background = ContextCompat.getDrawable(context, R.color.white)
        }
    }
}