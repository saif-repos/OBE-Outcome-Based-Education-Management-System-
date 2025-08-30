package com.example.obe_mngt_sys.ACTIVITIES

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.PloResultResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class ClickONCourseCLOBASEDRESULT : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var scrollContainer: ScrollView
    private lateinit var contentContainer: LinearLayout
    private lateinit var tvHeader: TextView
    private lateinit var tvSubHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_oncourse_clobasedresult)

        initViews()
        setupHeaders()

        val courseCode = intent.getStringExtra("CourseCode") ?: run {
            showError("Course code not provided")
            return
        }

        fetchCourseCloResults(
            intent.getStringExtra("UserID") ?: "",
            courseCode
        )
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        scrollContainer = findViewById(R.id.scrollContainer)
        contentContainer = findViewById(R.id.contentContainer)
        tvHeader = findViewById(R.id.tvHeader)
        tvSubHeader = findViewById(R.id.tvSubHeader)

        scrollContainer.isFocusable = true
        scrollContainer.isFocusableInTouchMode = true
    }

    private fun setupHeaders() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        val courseCode = intent.getStringExtra("CourseCode") ?: ""
        tvHeader.text = "CLO Results for $studentName"
        tvSubHeader.text = "Course: $courseCode"
    }

    private fun fetchCourseCloResults(studentId: String, courseCode: String) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        scrollContainer.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
                withContext(Dispatchers.Main) {
                    handlePloResponse(response, courseCode)
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

    private fun handlePloResponse(response: Response<List<PloResultResponse>>, targetCourseCode: String) {
        progressBar.visibility = View.GONE
        if (response.isSuccessful) {
            response.body()?.let { ploResults ->
                // Group CLOs by their ID to aggregate results from different PLOs
                val cloMap = mutableMapOf<Int, CourseCloResult>()

                ploResults.forEach { plo ->
                    plo.CourseBreakdown?.filter { it.CourseCode == targetCourseCode }
                        ?.flatMap { course ->
                            course.CloBreakdown?.map { clo ->
                                val existingClo = cloMap[clo.CloId]
                                if (existingClo == null) {
                                    cloMap[clo.CloId] = CourseCloResult(
                                        clo.CloId,
                                        clo.CloDescription,
                                        clo.AchievedWeightedScore,
                                        clo.WeightInCoursePlo,
                                        mutableListOf(plo.PloId))
                                } else {
                                    // If CLO exists, accumulate the scores and add PLO reference
                                    existingClo.achieved += clo.AchievedWeightedScore
                                    existingClo.weightage += clo.WeightInCoursePlo
                                    existingClo.relatedPlos.add(plo.PloId)
                                }
                            } ?: emptyList()
                        } ?: emptyList()
                }

                val courseClos = cloMap.values.toList()

                if (courseClos.isEmpty()) {
                    showError("No CLO data available for $targetCourseCode")
                } else {
                    displayCourseCloResults(courseClos, targetCourseCode)
                }
            } ?: showError("Empty response received")
        } else {
            showError("Server error: ${response.code()}")
        }
    }

    private fun displayCourseCloResults(cloResults: List<CourseCloResult>, courseCode: String) {
        runOnUiThread {
            scrollContainer.visibility = View.VISIBLE
            contentContainer.removeAllViews()

            // Add headers back
            contentContainer.addView(tvHeader)
            contentContainer.addView(tvSubHeader)

            // Create CLO table
            val cloTable = TableLayout(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, dpToPx(16), 0, 0)
            }

            // Add table headers
            val headerRow = TableRow(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            arrayOf("CLO ID", "Description", "Score", "Related PLOs").forEach { headerText ->
                headerRow.addView(createTableCell(headerText, true))
            }
            cloTable.addView(headerRow)

            // Add CLO rows
            cloResults.sortedBy { it.cloId }.forEach { clo ->
                val row = TableRow(this).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // CLO ID
                row.addView(createTableCell("CLO ${clo.cloId}", false).apply {
                    setTypeface(null, Typeface.BOLD)
                })

                // Description
                row.addView(createTableCell(clo.description ?: "N/A", false))

                // Score
                val ratio = "${"%.1f".format(clo.achieved)}/${"%.1f".format(clo.weightage)}"
                val percentage = if (clo.weightage > 0) (clo.achieved / clo.weightage) * 100 else 0.0
                val scoreText = "$ratio (${"%.1f".format(percentage)}%)"

                row.addView(createTableCell(scoreText, false))

                // Related PLOs
                val plosText = clo.relatedPlos.joinToString(", ") { "PLO $it" }
                row.addView(createTableCell(plosText, false))

                cloTable.addView(row)
            }

            contentContainer.addView(cloTable)
        }
    }

    private fun createTableCell(text: String, isHeader: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextAppearance(
                this@ClickONCourseCLOBASEDRESULT,
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
                    "Description" -> weight = 2f
                    "Score" -> weight = 1f
                    "Related PLOs" -> weight = 0.7f
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

    private data class CourseCloResult(
        val cloId: Int,
        val description: String?,
        var achieved: Double,
        var weightage: Double,
        val relatedPlos: MutableList<Int>
    )
}