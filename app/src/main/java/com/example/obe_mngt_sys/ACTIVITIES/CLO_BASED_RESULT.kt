package com.example.obe_mngt_sys.ACTIVITIES

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

class CLO_BASED_RESULT : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var scrollContainer: ScrollView
    private lateinit var contentContainer: LinearLayout // Changed from mainContainer
    private lateinit var tvHeader: TextView
    private lateinit var tvSubHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clo_based_result)

        initViews()
        setupHeaders()
        fetchCloResults(
            intent.getStringExtra("UserID") ?: "",
            intent.getIntExtra("PloId", 0)
        )
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        scrollContainer = findViewById(R.id.scrollContainer)
        contentContainer = findViewById(R.id.contentContainer) // Changed initialization
        tvHeader = findViewById(R.id.tvHeader)
        tvSubHeader = findViewById(R.id.tvSubHeader)

        // Ensure scrollContainer is properly initialized
        scrollContainer.isFocusable = true
        scrollContainer.isFocusableInTouchMode = true
    }


    private fun setupHeaders() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        val ploId = intent.getIntExtra("PloId", 0)
        tvHeader.text = "PLO $ploId Results for $studentName"
        tvSubHeader.text = "Courses and their CLO contributions to PLO$ploId"
    }

    private fun fetchCloResults(studentId: String, ploId: Int) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        scrollContainer.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
                withContext(Dispatchers.Main) {
                    handlePloResponse(response, ploId)
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

    private fun handlePloResponse(response: Response<List<PloResultResponse>>, targetPloId: Int) {
        progressBar.visibility = View.GONE
        if (response.isSuccessful) {
            response.body()?.let { ploResults ->
                val ploData = ploResults.find { it.PloId == targetPloId }
                if (ploData == null) {
                    showError("No data available for PLO$targetPloId")
                } else {
                    displayCloResults(ploData)
                }
            } ?: showError("Empty response received")
        } else {
            showError("Server error: ${response.code()}")
        }
    }

    private fun displayCloResults(ploData: PloResultResponse) {
        runOnUiThread {
            scrollContainer.visibility = View.VISIBLE
            contentContainer.removeAllViews() // Clear all views

            // Add headers back
            contentContainer.addView(tvHeader)
            contentContainer.addView(tvSubHeader)

            val filteredCourses = ploData.CourseBreakdown
                ?.filter { course ->
                    course.CloBreakdown?.any { it.WeightInCoursePlo > 0 } == true
                }
                ?: emptyList()

            if (filteredCourses.isEmpty()) {
                showError("No data available for PLO${ploData.PloId}")
                return@runOnUiThread
            }

            filteredCourses.forEach { course ->
                // Create course container
                val courseContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.border_rounded)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dpToPx(16)
                    }
                    setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
                }

                // Add course title
                val courseTitle = TextView(this).apply {
                    text = "${course.CourseCode} - ${course.CourseName}"
                    setTextAppearance(
                        this@CLO_BASED_RESULT,
                        com.google.android.material.R.style.TextAppearance_AppCompat_Small
                    )
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(this@CLO_BASED_RESULT, R.color.gray))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dpToPx(10)
                    }
                }
                courseContainer.addView(courseTitle)

                // Create CLO table
                val cloTable = TableLayout(this).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Add table headers
                val headerRow = TableRow(this).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                arrayOf("CLO ID", "DIFF_LEVEL", "Score").forEach { headerText ->
                    headerRow.addView(createTableCell(headerText, true))
                }
                cloTable.addView(headerRow)

                // Add CLO rows
                course.CloBreakdown
                    ?.filter { it.WeightInCoursePlo > 0 }
                    ?.forEach { clo ->
                        val row = TableRow(this).apply {
                            layoutParams = TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        // CLO ID
                        row.addView(createTableCell(clo.CloId.toString(), false).apply {
                            setTypeface(null, Typeface.BOLD)
                        })

                        // Description
                        row.addView(createTableCell(clo.Dlevel ?: "N/A", false))

                        // Score
                        val ratio =
                            "${"%.1f".format(clo.AchievedWeightedScore)}/${"%.1f".format(clo.WeightInCoursePlo)}"
                        val percentage = (clo.AchievedWeightedScore / clo.WeightInCoursePlo) * 100
                        val scoreText = "$ratio (${"%.1f".format(percentage)}%)"

                        row.addView(createTableCell(scoreText, false))

                        cloTable.addView(row)
                    }

                courseContainer.addView(cloTable)
                contentContainer.addView(courseContainer)
            }

        }
    }

    private fun createTableCell(text: String, isHeader: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextAppearance(
                this@CLO_BASED_RESULT,
                com.google.android.material.R.style.TextAppearance_AppCompat_Small
            )
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))

            if (isHeader) {
                setTypeface(null, Typeface.BOLD)
                setBackgroundResource(R.color.light_gray)
            } else {
                setBackgroundResource(R.color.white)
            }

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                when (text) {
                    "CLO ID" -> weight = 0.5f
                    "DIFFICULTY LEVEL" -> weight = 2f
                    "Score" -> weight = 1f
                }
            }
        }
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        scrollContainer.visibility = View.GONE
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}







