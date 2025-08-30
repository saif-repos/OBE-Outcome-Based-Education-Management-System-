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

class CLOBASEDALLPLOS : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var scrollContainer: ScrollView
    private lateinit var contentContainer: LinearLayout
    private lateinit var tvHeader: TextView
    private lateinit var tvSubHeader: TextView

    // Threshold for highlighting low scores
    private val lowScoreThreshold = 40.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clobasedallplos)

        initViews()
        setupHeaders()
        fetchAllPloResults(intent.getStringExtra("UserID") ?: "")
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        scrollContainer = findViewById(R.id.scrollContainer)
        contentContainer = findViewById(R.id.contentContainer)
        tvHeader = findViewById(R.id.tvHeader)
        tvSubHeader = findViewById(R.id.tvSubHeader)
    }

    private fun setupHeaders() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        tvHeader.text = "All PLOs CLO Results for $studentName"
        tvSubHeader.text = "Detailed CLO contributions for each PLO"
    }

    private fun fetchAllPloResults(studentId: String) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        scrollContainer.visibility = View.GONE

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
                    displayAllPloResults(ploResults.sortedBy { it.PloId })
                }
            } ?: showError("Empty response received")
        } else {
            showError("Server error: ${response.code()}")
        }
    }

    private fun displayAllPloResults(ploResults: List<PloResultResponse>) {
        runOnUiThread {
            scrollContainer.visibility = View.VISIBLE

            // Create a temporary container to build our content
            val tempContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Add headers to the temporary container
            val header = TextView(this).apply {
                text = tvHeader.text
                setTextAppearance(this@CLOBASEDALLPLOS, com.google.android.material.R.style.TextAppearance_AppCompat_Large)
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS, R.color.Aqua))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(8)
                }
            }
            tempContainer.addView(header)

            val subHeader = TextView(this).apply {
                text = tvSubHeader.text
                setTextAppearance(this@CLOBASEDALLPLOS, com.google.android.material.R.style.TextAppearance_AppCompat_Medium)
                setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS, R.color.gray))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(16)
                }
            }
            tempContainer.addView(subHeader)

            // Add all PLO data
            ploResults.forEach { ploData ->
                // Create PLO header
                val ploHeader = TextView(this).apply {
                    text = "PLO ${ploData.PloId}: ${ploData.PloDescription}"
                    setTextAppearance(this@CLOBASEDALLPLOS, com.google.android.material.R.style.TextAppearance_AppCompat_Large)
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS, R.color.Aqua))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dpToPx(16)
                        bottomMargin = dpToPx(8)
                    }
                }
                tempContainer.addView(ploHeader)

                // Add PLO score
                val ploScore = TextView(this).apply {
                    text = "Overall Score: ${"%.1f".format(ploData.FinalPloScore)}%"
                    setTextAppearance(this@CLOBASEDALLPLOS, com.google.android.material.R.style.TextAppearance_AppCompat_Medium)
                    setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS,
                        if (ploData.FinalPloScore >= 70) R.color.green else R.color.red))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dpToPx(16)
                    }
                }
                tempContainer.addView(ploScore)

                val filteredCourses = ploData.CourseBreakdown
                    ?.filter { course ->
                        course.CloBreakdown?.any { it.WeightInCoursePlo > 0 } == true
                    }
                    ?: emptyList()

                if (filteredCourses.isEmpty()) {
                    val noDataText = TextView(this).apply {
                        text = "No course data available for this PLO"
                        setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS, R.color.gray))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = dpToPx(24)
                        }
                    }
                    tempContainer.addView(noDataText)
                    return@forEach
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
                            this@CLOBASEDALLPLOS,
                            com.google.android.material.R.style.TextAppearance_AppCompat_Small
                        )
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(this@CLOBASEDALLPLOS, R.color.gray))
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = dpToPx(10)
                        }
                    }
                    courseContainer.addView(courseTitle)

                    // Add course contribution to PLO
                    val courseContribution = TextView(this).apply {
                        text = "Contribution to PLO: ${"%.1f".format(course.WeightageOfCourseInPlo)}%"
                        setTextAppearance(
                            this@CLOBASEDALLPLOS,
                            com.google.android.material.R.style.TextAppearance_AppCompat_Small
                        )
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = dpToPx(10)
                        }
                    }
                    courseContainer.addView(courseContribution)

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

                    arrayOf("CLO ID", "Description", "Score").forEach { headerText ->
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
                            val ratio = "${"%.1f".format(clo.AchievedWeightedScore)}/${"%.1f".format(clo.WeightInCoursePlo)}"
                            val percentage = (clo.AchievedWeightedScore / clo.WeightInCoursePlo) * 100
                            val scoreText = "$ratio (${"%.1f".format(percentage)}%)"

                            // Create score cell with conditional coloring
                            val scoreCell = createTableCell(scoreText, false).apply {
                                if (percentage < lowScoreThreshold) {
                                    setTextColor(ContextCompat.getColor(context, R.color.red))
                                    setTypeface(null, Typeface.BOLD)
                                }
                            }
                            row.addView(scoreCell)

                            cloTable.addView(row)
                        }

                    courseContainer.addView(cloTable)
                    tempContainer.addView(courseContainer)
                }
            }

            // Now replace the content
            contentContainer.removeAllViews()
            contentContainer.addView(tempContainer)
        }
    }

    private fun createTableCell(text: String, isHeader: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setTextAppearance(
                this@CLOBASEDALLPLOS,
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
                    "Difficulty level" -> weight = 2f
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