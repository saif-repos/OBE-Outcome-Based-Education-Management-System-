//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Typeface
//import android.os.Bundle
//import android.view.Gravity
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.R
//import com.example.obe_mngt_sys.MODELS.PloResultResponse
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.HttpException
//
//class STUDENT_DASHBOARD : AppCompatActivity() {
//    private lateinit var progressBar: ProgressBar
//    private lateinit var tvError: TextView
//    private lateinit var tableContainer: TableLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_student_dashboard)
//
//        initViews()
//        setupStudentInfo()
//        setupButtonClickListeners()
//        fetchPloResults(intent.getStringExtra("UserID") ?: "")
//    }
//
//    private fun initViews() {
//        progressBar = findViewById(R.id.progressBar)
//        tvError = findViewById(R.id.tvError)
//        tableContainer = findViewById(R.id.tableContainer)
//
//        // Make progress bar smaller
//        progressBar.layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        ).apply {
//            gravity = Gravity.CENTER
//        }
//    }
//
//    private fun setupStudentInfo() {
//        val studentName = intent.getStringExtra("UserName") ?: ""
//        findViewById<TextView>(R.id.textViewStudentName).text = studentName
//    }
//
//    private fun setupButtonClickListeners() {
//        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { navigateToLogin() }
//        findViewById<Button>(R.id.btnViewGrades).setOnClickListener {
//            navigateToViewGrades(
//                intent.getStringExtra("UserID") ?: "",
//                intent.getStringExtra("UserName") ?: ""
//            )
//        }
//    }
//
//    private fun navigateToLogin() {
//        val intent = Intent(this, Login::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//        startActivity(intent)
//        finish()
//    }
//
//    private fun navigateToViewGrades(studentId: String, studentName: String) {
//        startActivity(Intent(this, ViewGrades::class.java).apply {
//            putExtra("UserID", studentId)
//            putExtra("UserName", studentName)
//        })
//    }
//
//    private fun fetchPloResults(studentId: String) {
//        progressBar.visibility = View.VISIBLE
//        tvError.visibility = View.GONE
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
//                withContext(Dispatchers.Main) {
//                    handlePloResponse(response)
//                }
//            } catch (e: HttpException) {
//                withContext(Dispatchers.Main) {
//                    showError("HTTP error: ${e.message()}")
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showError("Error: ${e.message}")
//                }
//            }
//        }
//    }
//
//    private fun handlePloResponse(response: retrofit2.Response<List<PloResultResponse>>) {
//        progressBar.visibility = View.GONE
//        if (response.isSuccessful) {
//            response.body()?.let { ploResults ->
//                if (ploResults.isEmpty()) {
//                    showError("No PLO results found")
//                } else {
//                    displayPloResults(ploResults)
//                }
//            } ?: showError("Empty response received")
//        } else {
//            showError("Server error: ${response.code()}")
//        }
//    }
//
//    private fun showError(message: String) {
//        progressBar.visibility = View.GONE
//        tvError.text = message
//        tvError.visibility = View.VISIBLE
//    }
//
//    private fun displayPloResults(ploResults: List<PloResultResponse>) {
//        tableContainer.removeAllViews()
//        val (courses, plos, totals) = processDataForDisplay(ploResults)
//
//        addHeaderRow(courses, plos, totals)
//        courses.forEach { course -> addCourseRow(course, plos) }
//    }
//
//    private fun processDataForDisplay(ploResults: List<PloResultResponse>): Triple<List<DisplayCourse>, List<DisplayPlo>, List<Int>> {
//        // Ensure PLOs are sorted by ID for consistent display
//        val allPlos = ploResults.map { plo ->
//            DisplayPlo(plo.PloId, plo.PloDescription, plo.FinalPloScore)
//        }.sortedBy { it.id } // Sort by PLO ID
//
//        val allCourses = mutableListOf<DisplayCourse>().apply {
//            ploResults.forEach { plo ->
//                plo.CourseBreakdown?.forEach { course ->
//                    if (none { it.courseCode == course.CourseCode }) {
//                        add(DisplayCourse(course.CourseCode, course.CourseName, mutableMapOf()))
//                    }
//                }
//            }
//        }.sortedBy { it.courseCode } // Sort courses by code for consistent order
//
//        ploResults.forEach { plo ->
//            plo.CourseBreakdown?.forEach { course ->
//                allCourses.find { it.courseCode == course.CourseCode }?.let { displayCourse ->
//                    displayCourse.ploContributions[plo.PloId] =
//                        PloContribution(course.AchievedWeightage, course.WeightageOfCourseInPlo)
//                }
//            }
//        }
//
//        val totals = allPlos.map { Math.round(it.finalScore).toInt() }
//        return Triple(allCourses, allPlos, totals)
//    }
//
//    private fun addHeaderRow(courses: List<DisplayCourse>, plos: List<DisplayPlo>, totals: List<Int>) {
//        // Header Row
//        val headerRow = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        // Course Header
//        headerRow.addView(createHeaderCell("Course", true, plos.size))
//
//        // PLO Headers - Displaying just the PLO ID
//        plos.forEach { plo ->
//            headerRow.addView(createHeaderCell("${plo.id}", false, plos.size))
//        }
//
//        tableContainer.addView(headerRow)
//
//        // Totals Row
//        val totalsRow = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        // Total Label
//        totalsRow.addView(createHeaderCell("Total", true, plos.size))
//
//        // Total Values - Retaining conditional coloring for Total row
//        totals.forEach { total ->
//            totalsRow.addView(createHeaderCell("$total%", false, plos.size).apply {
//                setTextColor(
//                    if (total >= 70) ContextCompat.getColor(context, R.color.green)
//                    else ContextCompat.getColor(context, R.color.red)
//                )
//            })
//        }
//
//        tableContainer.addView(totalsRow)
//    }
//
//    private fun addCourseRow(course: DisplayCourse, plos: List<DisplayPlo>) {
//        val row = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        // Course Name
//        row.addView(createCell(course.courseName ?: "N/A", true, plos.size).apply {
//            setTypeface(null, Typeface.BOLD)
//        })
//
//        // PLO Contributions - No conditional coloring here
//        plos.forEach { plo ->
//            val contribution = course.ploContributions[plo.id]
//            row.addView(createCell(
//                contribution?.let { "${Math.round(it.achieved)}/${Math.round(it.weightage)}" } ?: "-",
//                false,
//                plos.size
//            ).apply {
//                // Conditional setTextColor for red/green removed as requested.
//                // The text will use the default color (typically black).
//            })
//        }
//
//        tableContainer.addView(row)
//    }
//
//    private fun createHeaderCell(text: String, isFirstColumn: Boolean, totalPloColumns: Int): TextView {
//        return TextView(this).apply {
//            setText(text)
//            setTextAppearance(android.R.style.TextAppearance_Small)
//            setTypeface(null, Typeface.BOLD)
//            gravity = Gravity.CENTER
//            setPadding(2, 4, 2, 4) // Reduced padding for compactness
//            textSize = 10f // Correct: Just the Float value for sp
//
//            layoutParams = TableRow.LayoutParams(
//                0, // Width will be determined by weight or direct assignment
//                TableRow.LayoutParams.WRAP_CONTENT,
//                1f // Base weight
//            ).apply {
//                if (isFirstColumn) {
//                    width = (resources.displayMetrics.widthPixels * 0.3).toInt() // Approximately 30% of screen width
//                    weight = 0f // No weight for fixed width
//                } else {
//                    // Calculate a desired minimum width in pixels (e.g., 50dp converted to pixels)
//                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
//                    val totalAvailablePloWidth = resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()
//
//                    // If total desired width of PLOs exceeds available space, set a fixed minimum width
//                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
//                        desiredPloWidthPx
//                    } else {
//                        0 // Let weight distribute if space allows
//                    }
//                    weight = if (width == 0) 1f else 0f // Use weight if not fixed, else no weight
//                }
//            }
//            background = ContextCompat.getDrawable(context, R.color.light_gray)
//        }
//    }
//
//    private fun createCell(text: String, isFirstColumn: Boolean, totalPloColumns: Int): TextView {
//        return TextView(this).apply {
//            setText(text)
//            setTextAppearance(android.R.style.TextAppearance_Small)
//            gravity = Gravity.CENTER
//            setPadding(2, 4, 2, 4) // Reduced padding
//            textSize = 10f // Correct: Just the Float value for sp
//
//            layoutParams = TableRow.LayoutParams(
//                0,
//                TableRow.LayoutParams.WRAP_CONTENT,
//                1f
//            ).apply {
//                if (isFirstColumn) {
//                    width = (resources.displayMetrics.widthPixels * 0.3).toInt()
//                    weight = 0f
//                } else {
//                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
//                    val totalAvailablePloWidth = resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()
//
//                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
//                        desiredPloWidthPx
//                    } else {
//                        0
//                    }
//                    weight = if (width == 0) 1f else 0f
//                }
//            }
//            background = ContextCompat.getDrawable(context, R.color.white)
//        }
//    }
//
//    // Display model classes
//    private data class DisplayPlo(
//        val id: Int,
//        val description: String,
//        val finalScore: Double
//    )
//
//    private data class DisplayCourse(
//        val courseCode: String,
//        val courseName: String?,
//        val ploContributions: MutableMap<Int, PloContribution>
//    )
//
//    private data class PloContribution(
//        val achieved: Double,
//        val weightage: Double
//    )
//}
//











//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Typeface
//import android.os.Bundle
//import android.view.Gravity
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.R
//import com.example.obe_mngt_sys.MODELS.PloResultResponse
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.HttpException
//import retrofit2.Response
//
//class STUDENT_DASHBOARD : AppCompatActivity() {
//    private lateinit var progressBar: ProgressBar
//    private lateinit var tvError: TextView
//    private lateinit var tableContainer: TableLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_student_dashboard)
//
//        initViews()
//        setupStudentInfo()
//        setupButtonClickListeners()
//        fetchPloResults(intent.getStringExtra("UserID") ?: "")
//    }
//
//    private fun initViews() {
//        progressBar = findViewById(R.id.progressBar)
//        tvError = findViewById(R.id.tvError)
//        tableContainer = findViewById(R.id.tableContainer)
//
//        progressBar.layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        ).apply {
//            gravity = Gravity.CENTER
//        }
//    }
//
//    private fun setupStudentInfo() {
//        val studentName = intent.getStringExtra("UserName") ?: ""
//        findViewById<TextView>(R.id.textViewStudentName).text = studentName
//    }
//
//    private fun setupButtonClickListeners() {
//        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { navigateToMianBoard() }
//        findViewById<Button>(R.id.btnViewGrades).setOnClickListener {
//            navigateToViewGrades(
//                intent.getStringExtra("UserID") ?: "",
//                intent.getStringExtra("UserName") ?: ""
//            )
//        }
//    }
//
//    //
////    private fun navigateToMianBoard() {
////        val intent = Intent(this, PLOBASEDRESULT_OVERALL::class.java).apply {
////            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
////        }
////        startActivity(intent)
////        finish()
////    }
//    private fun navigateToMianBoard() {
//        val studentId = intent.getStringExtra("UserID") ?: ""
//        val studentName = intent.getStringExtra("UserName") ?: ""
//
//        val intent = Intent(this, PLOBASEDRESULT_OVERALL::class.java).apply {
//            putExtra("UserID", studentId)
//            putExtra("UserName", studentName)
//            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//        }
//        startActivity(intent)
//        finish()
//    }
//
//    private fun navigateToViewGrades(studentId: String, studentName: String) {
//        startActivity(Intent(this, ViewGrades::class.java).apply {
//            putExtra("UserID", studentId)
//            putExtra("UserName", studentName)
//        })
//    }
//
//    private fun navigateToCloBasedResult(studentId: String, studentName: String, ploId: Int) {
//        startActivity(Intent(this, CLO_BASED_RESULT::class.java).apply {
//            putExtra("UserID", studentId)
//            putExtra("UserName", studentName)
//            putExtra("PloId", ploId)
//        })
//    }
//
//
//
//    private fun fetchPloResults(studentId: String) {
//        progressBar.visibility = View.VISIBLE
//        tvError.visibility = View.GONE
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
//                withContext(Dispatchers.Main) {
//                    handlePloResponse(response)
//                }
//            } catch (e: HttpException) {
//                withContext(Dispatchers.Main) {
//                    showError("HTTP error: ${e.message()}")
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    showError("Error: ${e.message}")
//                }
//            }
//        }
//    }
//
//    private fun handlePloResponse(response: Response<List<PloResultResponse>>) {
//        progressBar.visibility = View.GONE
//        if (response.isSuccessful) {
//            response.body()?.let { ploResults ->
//                if (ploResults.isEmpty()) {
//                    showError("No PLO results found")
//                } else {
//                    displayPloResults(ploResults)
//                }
//            } ?: showError("Empty response received")
//        } else {
//            showError("Server error: ${response.code()}")
//        }
//    }
//
//    private fun showError(message: String) {
//        progressBar.visibility = View.GONE
//        tvError.text = message
//        tvError.visibility = View.VISIBLE
//    }
//
//    private fun displayPloResults(ploResults: List<PloResultResponse>) {
//        tableContainer.removeAllViews()
//        val (courses, plos, totals) = processDataForDisplay(ploResults)
//        addHeaderRow(courses, plos, totals)
//        courses.forEach { course -> addCourseRow(course, plos) }
//    }
//
//    private fun processDataForDisplay(ploResults: List<PloResultResponse>): Triple<List<DisplayCourse>, List<DisplayPlo>, List<Int>> {
//        val allPlos = ploResults.map { plo ->
//            DisplayPlo(plo.PloId, plo.PloDescription, plo.FinalPloScore)
//        }.sortedBy { it.id }
//
//        val allCourses = mutableListOf<DisplayCourse>().apply {
//            ploResults.forEach { plo ->
//                plo.CourseBreakdown?.forEach { course ->
//                    if (none { it.courseCode == course.CourseCode }) {
//                        add(DisplayCourse(course.CourseCode, course.CourseName, mutableMapOf()))
//                    }
//                }
//            }
//        }.sortedBy { it.courseCode }
//
//        ploResults.forEach { plo ->
//            plo.CourseBreakdown?.forEach { course ->
//                allCourses.find { it.courseCode == course.CourseCode }?.let { displayCourse ->
//                    displayCourse.ploContributions[plo.PloId] =
//                        PloContribution(course.AchievedWeightage, course.WeightageOfCourseInPlo)
//                }
//            }
//        }
//
//        val totals = allPlos.map { Math.round(it.finalScore).toInt() }
//        return Triple(allCourses, allPlos, totals)
//    }
//
//    private fun addHeaderRow(
//        courses: List<DisplayCourse>,
//        plos: List<DisplayPlo>,
//        totals: List<Int>
//    ) {
//        val headerRow = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        headerRow.addView(createHeaderCell("Course", true, plos.size))
//
//        plos.forEach { plo ->
//            val ploCell = createHeaderCell("${plo.id}", false, plos.size).apply {
//                setOnClickListener {
//                    navigateToCloBasedResult(
//                        intent.getStringExtra("UserID") ?: "",
//                        intent.getStringExtra("UserName") ?: "",
//                        plo.id
//                    )
//                }
//                setBackgroundResource(R.drawable.plo_header_selector)
//            }
//            headerRow.addView(ploCell)
//        }
//
//        tableContainer.addView(headerRow)
//
//        val totalsRow = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        totalsRow.addView(createHeaderCell("Total", true, plos.size))
//        totals.forEach { total ->
//            totalsRow.addView(createHeaderCell("$total%", false, plos.size).apply {
//                setTextColor(
//                    if (total >= 70) ContextCompat.getColor(context, R.color.green)
//                    else ContextCompat.getColor(context, R.color.red)
//                )
//            })
//        }
//        tableContainer.addView(totalsRow)
//    }
//
//    private fun addCourseRow(course: DisplayCourse, plos: List<DisplayPlo>) {
//        val row = TableRow(this).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//        }
//
//        row.addView(createCell(course.courseName ?: "N/A", true, plos.size).apply {
//            setTypeface(null, Typeface.BOLD)
//        })
//
//        plos.forEach { plo ->
//            val contribution = course.ploContributions[plo.id]
//            row.addView(createCell(
//                contribution?.let { "${Math.round(it.achieved)}/${Math.round(it.weightage)}" }
//                    ?: "-",
//                false,
//                plos.size
//            ))
//        }
//
//        tableContainer.addView(row)
//    }
//
//    private fun createHeaderCell(
//        text: String,
//        isFirstColumn: Boolean,
//        totalPloColumns: Int
//    ): TextView {
//        return TextView(this).apply {
//            setText(text)
//            setTextAppearance(android.R.style.TextAppearance_Small)
//            setTypeface(null, Typeface.BOLD)
//            gravity = Gravity.CENTER
//            setPadding(2, 4, 2, 4)
//            textSize = 10f
//
//            layoutParams = TableRow.LayoutParams(
//                0,
//                TableRow.LayoutParams.WRAP_CONTENT,
//                1f
//            ).apply {
//                if (isFirstColumn) {
//                    width = (resources.displayMetrics.widthPixels * 0.3).toInt()
//                    weight = 0f
//                } else {
//                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
//                    val totalAvailablePloWidth =
//                        resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()
//
//                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
//                        desiredPloWidthPx
//                    } else {
//                        0
//                    }
//                    weight = if (width == 0) 1f else 0f
//                }
//            }
//            background = ContextCompat.getDrawable(context, R.color.light_gray)
//        }
//    }
//
//    private fun createCell(text: String, isFirstColumn: Boolean, totalPloColumns: Int): TextView {
//        return TextView(this).apply {
//            setText(text)
//            setTextAppearance(android.R.style.TextAppearance_Small)
//            gravity = Gravity.CENTER
//            setPadding(2, 4, 2, 4)
//            textSize = 10f
//
//            layoutParams = TableRow.LayoutParams(
//                0,
//                TableRow.LayoutParams.WRAP_CONTENT,
//                1f
//            ).apply {
//                if (isFirstColumn) {
//                    width = (resources.displayMetrics.widthPixels * 0.3).toInt()
//                    weight = 0f
//                } else {
//                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
//                    val totalAvailablePloWidth =
//                        resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()
//
//                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
//                        desiredPloWidthPx
//                    } else {
//                        0
//                    }
//                    weight = if (width == 0) 1f else 0f
//                }
//            }
//            background = ContextCompat.getDrawable(context, R.color.white)
//        }
//    }
//
//    private data class DisplayPlo(
//        val id: Int,
//        val description: String,
//        val finalScore: Double
//    )
//
//    private data class DisplayCourse(
//        val courseCode: String,
//        val courseName: String?,
//        val ploContributions: MutableMap<Int, PloContribution>
//    )
//
//    private data class PloContribution(
//        val achieved: Double,
//        val weightage: Double
//    )
//}




package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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

class STUDENT_DASHBOARD : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tableContainer: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        initViews()
        setupStudentInfo()
        setupButtonClickListeners()
        fetchPloResults(intent.getStringExtra("UserID") ?: "")
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        tableContainer = findViewById(R.id.tableContainer)

        progressBar.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }

    private fun setupStudentInfo() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        findViewById<TextView>(R.id.textViewStudentName).text = studentName
    }

    private fun setupButtonClickListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { navigateToMianBoard() }
        findViewById<Button>(R.id.btnViewGrades).setOnClickListener {
            navigateToViewGrades(
                intent.getStringExtra("UserID") ?: "",
                intent.getStringExtra("UserName") ?: ""
            )
        }
    }

    private fun navigateToMianBoard() {
        val studentId = intent.getStringExtra("UserID") ?: ""
        val studentName = intent.getStringExtra("UserName") ?: ""

        val intent = Intent(this, PLOBASEDRESULT_OVERALL::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToViewGrades(studentId: String, studentName: String) {
        startActivity(Intent(this, ViewGrades::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
        })
    }

    private fun navigateToCloBasedResultForPlo(studentId: String, studentName: String, ploId: Int) {
        startActivity(Intent(this, CLO_BASED_RESULT::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
            putExtra("PloId", ploId)
        })
    }

    private fun navigateToCloBasedResultForCourse(studentId: String, studentName: String, courseCode: String) {
        startActivity(Intent(this, ClickONCourseCLOBASEDRESULT::class.java).apply {
            putExtra("UserID", studentId)
            putExtra("UserName", studentName)
            putExtra("CourseCode", courseCode)
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
                    displayPloResults(ploResults)
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

    private fun displayPloResults(ploResults: List<PloResultResponse>) {
        tableContainer.removeAllViews()
        val (courses, plos, totals) = processDataForDisplay(ploResults)
        addHeaderRow(courses, plos, totals)
        courses.forEach { course -> addCourseRow(course, plos) }
    }

    private fun processDataForDisplay(ploResults: List<PloResultResponse>): Triple<List<DisplayCourse>, List<DisplayPlo>, List<Int>> {
        val allPlos = ploResults.map { plo ->
            DisplayPlo(plo.PloId, plo.PloDescription, plo.FinalPloScore)
        }.sortedBy { it.id }

        val allCourses = mutableListOf<DisplayCourse>().apply {
            ploResults.forEach { plo ->
                plo.CourseBreakdown?.forEach { course ->
                    if (none { it.courseCode == course.CourseCode }) {
                        add(DisplayCourse(course.CourseCode, course.CourseName, mutableMapOf()))
                    }
                }
            }
        }.sortedBy { it.courseCode }

        ploResults.forEach { plo ->
            plo.CourseBreakdown?.forEach { course ->
                allCourses.find { it.courseCode == course.CourseCode }?.let { displayCourse ->
                    displayCourse.ploContributions[plo.PloId] =
                        PloContribution(course.AchievedWeightage, course.WeightageOfCourseInPlo)
                }
            }
        }

        val totals = allPlos.map { Math.round(it.finalScore).toInt() }
        return Triple(allCourses, allPlos, totals)
    }

    private fun addHeaderRow(
        courses: List<DisplayCourse>,
        plos: List<DisplayPlo>,
        totals: List<Int>
    ) {
        val headerRow = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        headerRow.addView(createHeaderCell("Course", true, plos.size))

        plos.forEach { plo ->
            val ploCell = createHeaderCell("${plo.id}", false, plos.size).apply {
                setOnClickListener {
                    navigateToCloBasedResultForPlo(
                        intent.getStringExtra("UserID") ?: "",
                        intent.getStringExtra("UserName") ?: "",
                        plo.id
                    )
                }
                setBackgroundResource(R.drawable.plo_header_selector)
            }
            headerRow.addView(ploCell)
        }

        tableContainer.addView(headerRow)

        val totalsRow = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        totalsRow.addView(createHeaderCell("Total", true, plos.size))
        totals.forEach { total ->
            totalsRow.addView(createHeaderCell("$total%", false, plos.size).apply {
                setTextColor(
                    if (total >= 70) ContextCompat.getColor(context, R.color.green)
                    else ContextCompat.getColor(context, R.color.red)
                )
            })
        }
        tableContainer.addView(totalsRow)
    }

    private fun addCourseRow(course: DisplayCourse, plos: List<DisplayPlo>) {
        val row = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Make course name clickable
        val courseCell = createCell(course.courseName ?: "N/A", true, plos.size).apply {
            setTypeface(null, Typeface.BOLD)
            setOnClickListener {
                navigateToCloBasedResultForCourse(
                    intent.getStringExtra("UserID") ?: "",
                    intent.getStringExtra("UserName") ?: "",
                    course.courseCode
                )
            }
            setBackgroundResource(R.drawable.course_header_selector)
        }
        row.addView(courseCell)

        plos.forEach { plo ->
            val contribution = course.ploContributions[plo.id]
            row.addView(createCell(
                contribution?.let { "${Math.round(it.achieved)}/${Math.round(it.weightage)}" }
                    ?: "-",
                false,
                plos.size
            ))
        }

        tableContainer.addView(row)
    }

    private fun createHeaderCell(
        text: String,
        isFirstColumn: Boolean,
        totalPloColumns: Int
    ): TextView {
        return TextView(this).apply {
            setText(text)
            setTextAppearance(android.R.style.TextAppearance_Small)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(2, 4, 2, 4)
            textSize = 10f

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                if (isFirstColumn) {
                    width = (resources.displayMetrics.widthPixels * 0.3).toInt()
                    weight = 0f
                } else {
                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
                    val totalAvailablePloWidth =
                        resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()

                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
                        desiredPloWidthPx
                    } else {
                        0
                    }
                    weight = if (width == 0) 1f else 0f
                }
            }
            background = ContextCompat.getDrawable(context, R.color.light_gray)
        }
    }

    private fun createCell(text: String, isFirstColumn: Boolean, totalPloColumns: Int): TextView {
        return TextView(this).apply {
            setText(text)
            setTextAppearance(android.R.style.TextAppearance_Small)
            gravity = Gravity.CENTER
            setPadding(2, 4, 2, 4)
            textSize = 10f

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                if (isFirstColumn) {
                    width = (resources.displayMetrics.widthPixels * 0.3).toInt()
                    weight = 0f
                } else {
                    val desiredPloWidthPx = (resources.displayMetrics.density * 50).toInt()
                    val totalAvailablePloWidth =
                        resources.displayMetrics.widthPixels - (resources.displayMetrics.widthPixels * 0.3).toInt()

                    width = if (totalPloColumns * desiredPloWidthPx > totalAvailablePloWidth) {
                        desiredPloWidthPx
                    } else {
                        0
                    }
                    weight = if (width == 0) 1f else 0f
                }
            }
            background = ContextCompat.getDrawable(context, R.color.white)
        }
    }

    private data class DisplayPlo(
        val id: Int,
        val description: String,
        val finalScore: Double
    )

    private data class DisplayCourse(
        val courseCode: String,
        val courseName: String?,
        val ploContributions: MutableMap<Int, PloContribution>
    )

    private data class PloContribution(
        val achieved: Double,
        val weightage: Double
    )
}