package com.example.obe_mngt_sys.ACTIVITIES

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import kotlin.math.min
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.PdfUtils
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.MODELS.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.FileOutputStream

class FCR : Fragment() {
    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var semester: Int = 0
    private var offeredCourseId: Int = -1
    private var teacherId: String? = null

    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var courseTitle: TextView

    private lateinit var apiService: ApiService
    private var courseDetails: OfferedCourseDetailsResponse? = null
    private var plos: List<PLOResponse> = emptyList()
    private var ploMapClos: List<PloMapCloResponse> = emptyList()
    private var gradeDistribution: Map<String, Int>? = null
    private var cloGrades: List<CloGradeResponse> = emptyList()

    private val cloPloMap = mutableMapOf<Int, MutableSet<Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString(ARG_COURSE_CODE)
            courseName = it.getString(ARG_COURSE_NAME)
            section = it.getString(ARG_SECTION)
            semester = it.getInt(ARG_SEMESTER)
            offeredCourseId = it.getInt(ARG_OFFERED_COURSE_ID)
            teacherId = it.getString(ARG_TEACHER_ID)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.100.66/api_obe/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_f_c_r, container, false)

        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        errorText = view.findViewById(R.id.errorText)
        courseTitle = view.findViewById(R.id.courseTitle)

        view.findViewById<Button>(R.id.exportPdfButton).setOnClickListener { v ->
            val button = v as Button
            button.isEnabled = false
            button.text = "Generating PDF..."

            view.post {
                try {
                    // Unique file name for PDF
                    val fileName = "FCR_${courseCode}_${section}_${System.currentTimeMillis()}"

                    // ScrollView containing all the content
                    val scrollView = view.findViewById<ScrollView>(R.id.fcr_root_layout)

                    // List of all horizontal scroll views to handle scrollbar disabling
                    val horizontalScrollViews = listOf(
                        view.findViewById<HorizontalScrollView>(R.id.horizontalScrollView1),
                        view.findViewById<HorizontalScrollView>(R.id.horizontalScrollView2),
                        view.findViewById<HorizontalScrollView>(R.id.horizontalScrollView3),
                        view.findViewById<HorizontalScrollView>(R.id.horizontalScrollView4)
                    ).filterNotNull()

                    // Generate PDF using PdfUtils
                    val file = PdfUtils.generatePdfFromScrollView(
                        scrollView = scrollView,
                        context = requireContext(),
                        fileName = fileName,
                        horizontalScrollViews = horizontalScrollViews
                    )

                    if (file != null && file.exists()) {
                        Toast.makeText(context, "PDF saved successfully", Toast.LENGTH_SHORT).show()
                        openPdfFile(file)
                    } else {
                        Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("PDF Generation", "Error generating PDF", e)
                } finally {
                    button.isEnabled = true
                    button.text = "Export to PDF"
                }
            }
        }



        fetchData()
        return view
    }

    private fun openPdfFile(pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Verify there's an app to handle this intent
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                val chooserIntent = Intent.createChooser(intent, "Open PDF with")
                startActivity(chooserIntent)
            } else {
                Toast.makeText(context,
                    "No PDF viewer found. Saved to: ${pdfFile.path}",
                    Toast.LENGTH_LONG).show()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context,
                "No PDF viewer app installed",
                Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context,
                "Error opening PDF: ${e.localizedMessage}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchData() {
        showLoading()

        val courseDetailsCall = apiService.getOfferedCourseDetails(offeredCourseId)
        val plosCall = apiService.getProgramPlosByOfferedCourse(offeredCourseId)
        val ploMapClosCall = apiService.getPloMapCloRecords(offeredCourseId)
        val gradeDistributionCall = apiService.getCourseGradeDistribution(offeredCourseId)
        val cloGradesCall = apiService.getAllCloWithGrades(offeredCourseId)

        courseDetailsCall.enqueue(object : Callback<OfferedCourseDetailsResponse> {
            override fun onResponse(call: Call<OfferedCourseDetailsResponse>, response: Response<OfferedCourseDetailsResponse>) {
                if (response.isSuccessful) {
                    courseDetails = response.body()
                    updateUI()
                } else {
                    showError("Failed to fetch course details")
                }
            }

            override fun onFailure(call: Call<OfferedCourseDetailsResponse>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })

        plosCall.enqueue(object : Callback<List<PLOResponse>> {
            override fun onResponse(call: Call<List<PLOResponse>>, response: Response<List<PLOResponse>>) {
                if (response.isSuccessful) {
                    plos = response.body() ?: emptyList()
                    updateUI()
                } else {
                    showError("Failed to fetch PLOs")
                }
            }

            override fun onFailure(call: Call<List<PLOResponse>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })

        ploMapClosCall.enqueue(object : Callback<List<PloMapCloResponse>> {
            override fun onResponse(call: Call<List<PloMapCloResponse>>, response: Response<List<PloMapCloResponse>>) {
                if (response.isSuccessful) {
                    ploMapClos = response.body() ?: emptyList()
                    processPloMappings()
                    updateUI()
                } else {
                    showError("Failed to fetch PLO-CLO mappings")
                }
            }

            override fun onFailure(call: Call<List<PloMapCloResponse>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })

        gradeDistributionCall.enqueue(object : Callback<GradeDistributionResponse> {
            override fun onResponse(call: Call<GradeDistributionResponse>, response: Response<GradeDistributionResponse>) {
                if (response.isSuccessful) {
                    gradeDistribution = response.body()?.gradeDistribution
                    updateUI()
                } else {
                    if (response.code() == 404) {
                        gradeDistribution = null
                        updateUI()
                    } else {
                        showError("Failed to fetch grade distribution")
                    }
                }
            }

            override fun onFailure(call: Call<GradeDistributionResponse>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })

        cloGradesCall.enqueue(object : Callback<List<CloGradeResponse>> {
            override fun onResponse(call: Call<List<CloGradeResponse>>, response: Response<List<CloGradeResponse>>) {
                if (response.isSuccessful) {
                    cloGrades = response.body() ?: emptyList()
                    updateUI()
                } else {
                    if (response.code() == 404) {
                        cloGrades = emptyList()
                        updateUI()
                    } else {
                        showError("Failed to fetch CLO grades")
                    }
                }
            }

            override fun onFailure(call: Call<List<CloGradeResponse>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun processPloMappings() {
        cloPloMap.clear()
        ploMapClos.forEach { item ->
            if (item.per != 0) {
                if (!cloPloMap.containsKey(item.cloId)) {
                    cloPloMap[item.cloId] = mutableSetOf()
                }
                cloPloMap[item.cloId]?.add(item.ploId)
            }
        }
    }

    private fun updateUI() {
        if (courseDetails == null || plos.isEmpty()) {
            showError("No course details or PLOs found")
            return
        }

        hideLoading()
        courseTitle.text = "Course: ${courseDetails?.courseName}"
        renderPLOs()
        renderCloPloMapping()
        renderMetricGoals()
        renderCourseInfo()
        renderGradeDistribution()
        renderCloGrades()
        renderCloPloResults()
    }

    private fun renderPLOs() {
        val ploContainer = view?.findViewById<LinearLayout>(R.id.ploContainer)
        ploContainer?.removeAllViews()

        plos.forEachIndexed { index, plo ->
            val row = createTableRow()

            val letterCell = createTextView(ploLetter(index), 30, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
            letterCell.setTypeface(null, android.graphics.Typeface.BOLD)
            row.addView(letterCell)

            val descCell = createTextView(plo.description, 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_TEXT_START)
            descCell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            row.addView(descCell)

            ploContainer?.addView(row)
        }
    }

    private fun renderCloPloMapping() {
        val container = view?.findViewById<LinearLayout>(R.id.cloPloMappingContainer)
        container?.removeAllViews()

        // Group CLOs with their PLO mappings
        // Group CLOs with their PLO mappings
        val groupedClos = mutableMapOf<Int, CloPloMapping>()
        ploMapClos.forEach { item ->
            if (!groupedClos.containsKey(item.cloId)) {
                groupedClos[item.cloId] = CloPloMapping(
                    cloId = item.cloId,
                    description = item.cloInfo.description, // Now using the correct path
                    plos = mutableMapOf()
                )
            }
            if (item.per != 0) {  // Changed to 0.0 since per is now Double
                groupedClos[item.cloId]?.plos?.put(item.ploId, "X")
            }
        }


        if (groupedClos.isEmpty()) return

        // Header Row 1
        val headerRow1 = createTableRow()
        headerRow1.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        val cloDescHeader = createTextView("Course Learning Outcomes", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER)
        cloDescHeader.setTypeface(null, android.graphics.Typeface.BOLD)
        cloDescHeader.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        headerRow1.addView(cloDescHeader)

        val ploHeader = createTextView("Program Learning Outcomes", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER)
        ploHeader.setTypeface(null, android.graphics.Typeface.BOLD)
        ploHeader.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            7f
        )
        headerRow1.addView(ploHeader)

        container?.addView(headerRow1)

        // Header Row 2 (PLO letters)
        val headerRow2 = createTableRow()
        headerRow2.setBackgroundColor(resources.getColor(android.R.color.background_light))

        val emptyCell = createTextView("", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER)
        emptyCell.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        headerRow2.addView(emptyCell)

        plos.forEach { plo ->
            val ploLetterCell = createTextView(ploLetter(plos.indexOf(plo)), 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER)
            ploLetterCell.setTypeface(null, android.graphics.Typeface.BOLD)
            ploLetterCell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7f / plos.size
            )
            headerRow2.addView(ploLetterCell)
        }

        container?.addView(headerRow2)

        // Data Rows
        groupedClos.values.forEach { clo ->
            val dataRow = createTableRow()

            val cloDescCell = createTextView(clo.description, 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_TEXT_START)
            cloDescCell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
            dataRow.addView(cloDescCell)

            plos.forEach { plo ->
                val mappingCell = createTextView(clo.plos[plo.ploId] ?: "", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER)
                mappingCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    7f / plos.size
                )
                dataRow.addView(mappingCell)
            }

            container?.addView(dataRow)
        }
    }

    private fun renderMetricGoals() {
        val container = view?.findViewById<LinearLayout>(R.id.metricGoalsContainer)
        container?.removeAllViews()

        // Get PLOs with actual mappings
        val plosWithMappings = ploMapClos.filter { it.per != 0 }.map { it.ploId }.toSet()
        val mappedPlos = plos.filter { plosWithMappings.contains(it.ploId) }

        if (mappedPlos.isEmpty()) return

        // Create a parent layout that will match parent width
        val tableLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Header Row 1
        val headerRow1 = createTableRow().apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }

        val emptyHeader = createTextView("", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        headerRow1.addView(emptyHeader)

        val ploHeader = createTextView("Program Learning Outcomes", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
        }
        headerRow1.addView(ploHeader)

        tableLayout.addView(headerRow1)

        // Header Row 2 (PLO letters)
        val headerRow2 = createTableRow().apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(resources.getColor(android.R.color.background_light))
        }

        val emptyCell = createTextView("", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        headerRow2.addView(emptyCell)

        mappedPlos.forEach { plo ->
            val ploLetterCell = createTextView(ploLetter(plos.indexOf(plo)), 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3f / mappedPlos.size
                )
            }
            headerRow2.addView(ploLetterCell)
        }

        tableLayout.addView(headerRow2)

        // Data Row (Metric Goals)
        val dataRow = createTableRow().apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val metricGoalLabel = createTextView("Metric Goal", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_TEXT_START).apply {
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        dataRow.addView(metricGoalLabel)

        mappedPlos.forEach { _ ->
            val goalCell = createTextView("2.0", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3f / mappedPlos.size
                )
            }
            dataRow.addView(goalCell)
        }

        tableLayout.addView(dataRow)
        container?.addView(tableLayout)
    }

    private fun renderCourseInfo() {
        val container = view?.findViewById<LinearLayout>(R.id.courseInfoContainer)
        container?.removeAllViews()

        courseDetails?.let { details ->
            addInfoRow(container, "Course Code & Number", details.courseCode)
            addInfoRow(container, "Sections", "${details.programName}-${details.semester}${details.section}")
            addInfoRow(container, "Total Credits", details.creditHours.toString())
            addInfoRow(container, "Instructor", details.teacherName)
            addInfoRow(container, "Course Title", details.courseName)
            addInfoRow(container, "Academic Term", "${details.session}${details.year}")
        }
    }

    private fun addInfoRow(container: LinearLayout?, label: String, value: String?) {
        val row = createTableRow()

        val labelView = createTextView(label, 0, 12, 12, 14, 12, View.TEXT_ALIGNMENT_TEXT_START)
        labelView.setTypeface(null, android.graphics.Typeface.BOLD)
        labelView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        row.addView(labelView)

        val valueView = createTextView(value ?: "N/A", 0, 12, 12, 14, 12, View.TEXT_ALIGNMENT_TEXT_END)
        valueView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        row.addView(valueView)

        container?.addView(row)
    }

    private fun renderGradeDistribution() {
        val container = view?.findViewById<LinearLayout>(R.id.gradeDistributionContainer)
        container?.removeAllViews()

        gradeDistribution?.let { grades ->
            // Create a parent layout that will match parent width
            val tableLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            // Header Row
            val headerRow = createTableRow().apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            }

            val subjectHeader = createTextView("Subject Grades", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2f
                )
            }
            headerRow.addView(subjectHeader)

            listOf("A", "B", "C", "D").forEach { grade ->
                val gradeHeader = createTextView(grade, 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                    setTypeface(null, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                headerRow.addView(gradeHeader)
            }

            tableLayout.addView(headerRow)

            // Data Row
            val dataRow = createTableRow().apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val totalLabel = createTextView("Total", 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2f
                )
            }
            dataRow.addView(totalLabel)

            listOf("A", "B", "C", "D").forEach { grade ->
                val count = grades[grade] ?: 0
                val countCell = createTextView(count.toString(), 0, 10, 10, 14, 10, View.TEXT_ALIGNMENT_CENTER).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }
                dataRow.addView(countCell)
            }

            tableLayout.addView(dataRow)
            container?.addView(tableLayout)
        }
    }

    private fun renderCloGrades() {
        val container = view?.findViewById<LinearLayout>(R.id.cloGradesContainer)
        container?.removeAllViews()

        cloGrades.forEachIndexed { cloIndex, clo ->
            val cloCard = LinearLayout(context)
            cloCard.orientation = LinearLayout.VERTICAL
            cloCard.setPadding(0, 0, 0, 16.dpToPx())

            val cloTitle = createTextView("${cloIndex + 1}. ${clo.description}", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_TEXT_START)
            cloTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            cloCard.addView(cloTitle)

            val innerTable = LinearLayout(context)
            innerTable.orientation = LinearLayout.VERTICAL
            innerTable.background = resources.getDrawable(R.drawable.border)

            // Header Row
            val headerRow = createTableRow()
            headerRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

            val headWiseHeader = createTextView("Head Wise Grades", 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
            headWiseHeader.setTypeface(null, android.graphics.Typeface.BOLD)
            headWiseHeader.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
            headerRow.addView(headWiseHeader)

            listOf("A", "B", "C", "D", "EEMU", "Avg.").forEach { header ->
                val headerCell = createTextView(header, 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
                headerCell.setTypeface(null, android.graphics.Typeface.BOLD)
                headerCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                headerRow.addView(headerCell)
            }

            innerTable.addView(headerRow)

            // Data Rows
            clo.grades.forEach { grade ->
                val dataRow = createTableRow()

                val taskIdentifier = when {
                    grade.taskType.equals("Lab", true) && grade.taskId != null ->
                        grade.taskId.replace("_", "")
                    grade.taskType.equals("Mid", true) -> "Mid Exam"
                    grade.taskType.equals("Final", true) -> "Final Exam"
                    grade.smTaskId != null -> {
                        val parts = grade.smTaskId.split("_")
                        if (parts.size > 1) "${grade.taskType} ${parts[1]}" else grade.taskType
                    }
                    grade.taskId != null -> grade.taskType
                    else -> grade.taskType
                }

                val taskCell = createTextView(taskIdentifier, 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_TEXT_START)
                taskCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3f
                )
                dataRow.addView(taskCell)

                listOf(grade.a, grade.b, grade.c, grade.d).forEach { count ->
                    val countCell = createTextView(count.toString(), 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
                    countCell.layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    dataRow.addView(countCell)
                }

                val eemuCell = createTextView("(${grade.a},${grade.b},${grade.c},${grade.d})", 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
                eemuCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                dataRow.addView(eemuCell)

                val avgCell = createTextView(String.format("%.2f", grade.taskAverage), 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
                avgCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                dataRow.addView(avgCell)

                innerTable.addView(dataRow)
            }

            // Average Row
            val avgRow = createTableRow()

            val avgLabel = createTextView("Avg.", 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_TEXT_END)
            avgLabel.setTypeface(null, android.graphics.Typeface.BOLD)
            avgLabel.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
            avgRow.addView(avgLabel)

            // Empty cells for A, B, C, D, EEMU
            repeat(5) {
                val emptyCell = createTextView("", 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
                emptyCell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                avgRow.addView(emptyCell)
            }

            val cloAvgCell = createTextView(String.format("%.2f", clo.cloAverage), 0, 8, 8, 12, 8, View.TEXT_ALIGNMENT_CENTER)
            cloAvgCell.setTypeface(null, android.graphics.Typeface.BOLD)
            cloAvgCell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            avgRow.addView(cloAvgCell)

            innerTable.addView(avgRow)
            cloCard.addView(innerTable)
            container?.addView(cloCard)
        }
    }

    private fun renderCloPloResults() {
        val container = view?.findViewById<LinearLayout>(R.id.cloPloResultContainer)
        container?.removeAllViews()

        // Get PLOs with actual mappings
        val plosWithMappings = ploMapClos.filter { it.per != 0 }.map { it.ploId }.toSet()
        val mappedPlos = plos.filter { plosWithMappings.contains(it.ploId) }

        if (mappedPlos.isEmpty() || cloGrades.isEmpty()) return

        // Header Row
        val headerRow = createTableRow()
        headerRow.setBackgroundColor(resources.getColor(android.R.color.white))

        val emptyHeader = createTextView("", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
        emptyHeader.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        headerRow.addView(emptyHeader)

        mappedPlos.forEach { plo ->
            val ploHeader = createTextView(ploLetter(plos.indexOf(plo)).toUpperCase(), 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
            ploHeader.setTypeface(null, android.graphics.Typeface.BOLD)
            ploHeader.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7f / mappedPlos.size
            )
            headerRow.addView(ploHeader)
        }

        container?.addView(headerRow)

        // CLO Data Rows
        cloGrades.forEachIndexed { cloIndex, clo ->
            val dataRow = createTableRow()

            val cloDesc = createTextView("${cloIndex + 1}. ${clo.description}", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_TEXT_START)
            cloDesc.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                3f
            )
            dataRow.addView(cloDesc)

            mappedPlos.forEach { plo ->
                val isMapped = cloPloMap[clo.cloId]?.contains(plo.ploId) ?: false
                val value = if (isMapped) String.format("%.2f", clo.cloAverage) else ""

                val cell = createTextView(value, 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
                cell.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    7f / mappedPlos.size
                )
                dataRow.addView(cell)
            }

            container?.addView(dataRow)
        }

        // EPAN (Average) Row
        val epanRow = createTableRow()
        epanRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        val epanLabel = createTextView("EPAN (Average)", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_TEXT_START)
        epanLabel.setTypeface(null, android.graphics.Typeface.BOLD)
        epanLabel.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        epanRow.addView(epanLabel)

        mappedPlos.forEach { plo ->
            val epanAvg = calculateEpanAverageForPlo(plo.ploId)
            val value = if (epanAvg != null) String.format("%.2f", epanAvg) else ""

            val cell = createTextView(value, 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
            cell.setTypeface(null, android.graphics.Typeface.BOLD)
            cell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7f / mappedPlos.size
            )
            epanRow.addView(cell)
        }

        container?.addView(epanRow)

        // BS-AI (Metric Goal) Row
        val metricGoalRow = createTableRow()
        metricGoalRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        val metricGoalLabel = createTextView("BS-AI (Metric Goal)", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_TEXT_START)
        metricGoalLabel.setTypeface(null, android.graphics.Typeface.BOLD)
        metricGoalLabel.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        metricGoalRow.addView(metricGoalLabel)

        mappedPlos.forEach { _ ->
            val cell = createTextView("2.0", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
            cell.setTypeface(null, android.graphics.Typeface.BOLD)
            cell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7f / mappedPlos.size
            )
            metricGoalRow.addView(cell)
        }

        container?.addView(metricGoalRow)

        // PROFICIENT Row
        val proficientRow = createTableRow()

        val emptyCell = createTextView("", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_TEXT_START)
        emptyCell.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3f
        )
        proficientRow.addView(emptyCell)

        mappedPlos.forEach { plo ->
            val epanAvg = calculateEpanAverageForPlo(plo.ploId)
            val isProficient = epanAvg != null && epanAvg > 2.0

            val cell = createTextView(if (isProficient) "PROFICIENT" else "", 0, 8, 8, 14, 8, View.TEXT_ALIGNMENT_CENTER)
            cell.setTypeface(null, android.graphics.Typeface.BOLD)
            if (isProficient) {
                cell.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            }
            cell.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                7f / mappedPlos.size
            )
            proficientRow.addView(cell)
        }

        container?.addView(proficientRow)
    }

    private fun calculateEpanAverageForPlo(ploId: Int): Double? {
        var total = 0.0
        var count = 0

        cloGrades.forEach { cloGrade ->
            if (cloPloMap[cloGrade.cloId]?.contains(ploId) == true) {
                total += cloGrade.cloAverage
                count++
            }
        }

        return if (count > 0) total / count else null
    }

    private fun ploLetter(index: Int): String {
        return ('a' + index).toString()
    }

    private fun createTableRow(): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 0, 0, 0)
        row.background = resources.getDrawable(R.drawable.border)
        return row
    }

    private fun createTextView(
        text: String,
        leftPadding: Int,
        topPadding: Int,
        rightPadding: Int,
        textSize: Int,
        bottomPadding: Int,
        alignment: Int
    ): TextView {
        val textView = TextView(context)
        textView.text = text
        textView.setPadding(
            leftPadding.dpToPx(),
            topPadding.dpToPx(),
            rightPadding.dpToPx(),
            bottomPadding.dpToPx()
        )
        textView.textSize = textSize.toFloat()
        textView.textAlignment = alignment
        return textView
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        errorText.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.GONE
    }

    private fun showError(message: String) {
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }
//    private fun exportToPdf() {
//        // 1. Get the root ScrollView (already has an ID)
//        val mainScrollView = view?.findViewById<ScrollView>(R.id.fcr_root_layout)
//
//        // 2. Get all HorizontalScrollViews (replace with your actual IDs)
//        val horizontalScrollViews = listOf(
//            view?.findViewById<HorizontalScrollView>(R.id.horizontalScrollView1),
//            view?.findViewById<HorizontalScrollView>(R.id.horizontalScrollView2),
//            view?.findViewById<HorizontalScrollView>(R.id.horizontalScrollView3)
//        ).filterNotNull() // Remove null entries (if any ID is wrong)
//
//        // 3. Check if views exist
//        if (mainScrollView == null || horizontalScrollViews.isEmpty()) {
//            Toast.makeText(context, "Could not find scroll views", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // 4. Generate PDF
//        val fileName = "FCR_${courseCode}_${section}_${System.currentTimeMillis()}"
//        val pdfFile = PdfUtils.generatePdfFromMultiScrollViews(
//            rootView = requireView(),
//            scrollViews = listOf(mainScrollView),
//            horizontalScrollViews = horizontalScrollViews,
//            context = requireContext(),
//            fileName = fileName
//        )
//
//        // 5. Show result
//        if (pdfFile != null) {
//            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
//        } else {
//            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
//        }
//    }
    data class CloPloMapping(
        val cloId: Int,
        val description: String,
        val plos: MutableMap<Int, String>
    )

    interface ApiService {
        @GET("api/Teacher/GetOfferedCourseDetails")
        fun getOfferedCourseDetails(@Query("oc_id") ocId: Int): Call<OfferedCourseDetailsResponse>

        @GET("api/Teacher/GetProgramPlosByOfferedCourse")
        fun getProgramPlosByOfferedCourse(@Query("oc_id") ocId: Int): Call<List<PLOResponse>>

        @GET("api/Teacher/GetPloMapCloRecords")
        fun getPloMapCloRecords(@Query("oc_id") ocId: Int): Call<List<PloMapCloResponse>>

        @GET("api/Teacher/GetCourseGradeDistribution")
        fun getCourseGradeDistribution(@Query("oc_id") ocId: Int): Call<GradeDistributionResponse>

        @GET("api/Teacher/GetAllCloWithGrades")
        fun getAllCloWithGrades(@Query("oc_id") ocId: Int): Call<List<CloGradeResponse>>
    }

    companion object {
        private const val ARG_COURSE_CODE = "courseCode"
        private const val ARG_COURSE_NAME = "courseName"
        private const val ARG_SECTION = "section"
        private const val ARG_SEMESTER = "semester"
        private const val ARG_OFFERED_COURSE_ID = "offeredCourseId"
        private const val ARG_TEACHER_ID = "teacherId"

        @JvmStatic
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            teacherId: String
        ) =
            FCR().apply {
                arguments = Bundle().apply {
                    putString(ARG_COURSE_CODE, courseCode)
                    putString(ARG_COURSE_NAME, courseName)
                    putString(ARG_SECTION, section)
                    putInt(ARG_SEMESTER, semester)
                    putInt(ARG_OFFERED_COURSE_ID, offeredCourseId)
                    putString(ARG_TEACHER_ID, teacherId)
                }
            }
    }
}