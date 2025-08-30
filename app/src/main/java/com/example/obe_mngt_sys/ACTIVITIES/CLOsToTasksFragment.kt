//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.content.res.ColorStateList
//import android.graphics.Color
//import android.graphics.Typeface
//import android.os.Bundle
//import android.text.InputType
//import android.util.Log
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.lifecycleScope
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.MODELS.ActivityType
//import com.example.obe_mngt_sys.MODELS.QuestionCloMappingsResponse
//import com.example.obe_mngt_sys.MODELS.UpdatePercentageRequest
//import com.example.obe_mngt_sys.R
//import kotlinx.coroutines.launch
//
//class CLOsToTasksFragment : Fragment() {
//
//    data class TaskMapping(
//        val TaskId: Int,
//        val TaskName: String,
//        val ActivityType: String,
//        val SmallTaskId: String?,
//        val Questions: List<QuestionMapping>
//    )
//
//    data class QuestionMapping(
//        val QuestionNo: String,
//        val QuestionId:Int,
//        val CLOs: List<CloMapping>
//    )
//
//    data class CloMapping(
//        val CLO_ID: Int,
//        val Percentage: String
//    )
//
//    private var courseCode: String? = null
//    private var courseName: String? = null
//    private var section: String? = null
//    private var teacherId: String = ""
//    private var semester: Int? = null
//    private var offeredCourseId: Int? = null
//    private lateinit var buttonsContainer: LinearLayout
//    private lateinit var mappingTable: TableLayout
//    private lateinit var editButton: Button
//    private var isEditMode = false
//    private var currentMappings: QuestionCloMappingsResponse? = null
//    private var currentTypeId: Int? = null // Keep track of the currently selected activity type
//
//    // Define standard padding and text sizes to ensure consistency
//    private val CELL_PADDING_DP = 8
//    private val HEADER_TEXT_SIZE_SP = 14f
//    private val CELL_TEXT_SIZE_SP = 12f
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            courseCode = it.getString("courseCode")
//            courseName = it.getString("courseName")
//            section = it.getString("section")
//            semester = it.getInt("semester")
//            offeredCourseId = it.getInt("offeredCourseId")
//            teacherId = it.getString("teacherId") ?: ""
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_c_l_os_to_tasks, container, false)
//        buttonsContainer = view.findViewById(R.id.buttons_container)
//        mappingTable = view.findViewById(R.id.mapping_table)
//        editButton = view.findViewById(R.id.editButton)
//        return view
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Set course info
//        val courseName = arguments?.getString("courseName") ?: "N/A"
//        val section = arguments?.getString("section") ?: "N/A"
//        val semester = arguments?.getInt("semester") ?: 0
//
//        view.findViewById<TextView>(R.id.textViewCourseName).text = "Course: $courseName"
//        view.findViewById<TextView>(R.id.textViewSection).text = "Section: $section"
//        view.findViewById<TextView>(R.id.textViewSemester).text = "Semester: $semester"
//
//        // Set up edit button
//        editButton.setOnClickListener { onEditClicked() }
//
//        loadActivities()
//    }
//
//    private fun loadActivities() {
//        offeredCourseId?.let { ocId ->
//            teacherId?.let { tId ->
//                lifecycleScope.launch {
//                    try {
//                        // Pass both oc_id and T_id to the API
//                        val response = RetrofitInstance.apiService.GetActivitiesbtn(ocId, tId)
//                        if (response.isSuccessful) {
//                            response.body()?.let { activities ->
//                                createActivityButtons(activities)
//                            }
//                        } else {
//                            showError("Failed to load activities")
//                        }
//                    } catch (e: Exception) {
//                        showError("Network error: ${e.message}")
//                    }
//                }
//            } ?: showError("Teacher ID not available")
//        } ?: showError("Course ID not available")
//    }
//
//    private fun createActivityButtons(activities: List<ActivityType>) {
//        activity?.runOnUiThread {
//            buttonsContainer.removeAllViews()
//
//            if (activities.isEmpty()) {
//                buttonsContainer.addView(TextView(requireContext()).apply {
//                    text = "No activities available"
//                    gravity = Gravity.CENTER
//                    setPadding(0, 16.dpToPx(), 0, 0)
//                })
//                return@runOnUiThread
//            }
//
//            val buttonParams = LinearLayout.LayoutParams(
//                0,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                weight = 1f
//                val marginInPx = 4.dpToPx()
//                setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
//            }
//
//            var currentRow: LinearLayout? = null
//
//            activities.forEachIndexed { index, activity ->
//                if (index % 3 == 0) {
//                    currentRow = LinearLayout(requireContext()).apply {
//                        orientation = LinearLayout.HORIZONTAL
//                        layoutParams = LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            LinearLayout.LayoutParams.WRAP_CONTENT
//                        )
//                        buttonsContainer.addView(this)
//                    }
//                }
//
//                val button = Button(requireContext()).apply {
//                    text = activity.Typee
//                    layoutParams = buttonParams
//                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#469FD1"))
//                    setTextColor(Color.WHITE)
//                    textSize = 12f
//                    setPadding(12.dpToPx(), 4.dpToPx(), 12.dpToPx(), 4.dpToPx())
//                    minWidth = 64.dpToPx()
//                    minHeight = 36.dpToPx()
//
//                    setOnClickListener {
//                        // Reset all buttons first
//                        resetButtonColors()
//                        // Set selected button color
//                        this.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2c7be5"))
//                        currentTypeId = activity.typeId
//                        loadQuestionMappings(activity.typeId)
//                    }
//                }
//
//                currentRow?.addView(button)
//            }
//        }
//    }
//
//    private fun resetButtonColors() {
//        for (i in 0 until buttonsContainer.childCount) {
//            val row = buttonsContainer.getChildAt(i) as? LinearLayout ?: continue
//            for (j in 0 until row.childCount) {
//                val button = row.getChildAt(j) as? Button ?: continue
//                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#469FD1"))
//            }
//        }
//    }
//
//    // Keep this extension function
//    private fun Int.dpToPx(): Int {
//        return (this * resources.displayMetrics.density).toInt()
//    }
//    private fun loadQuestionMappings(typeId: Int) {
//        offeredCourseId?.let { ocId ->
//            lifecycleScope.launch {
//                try {
//                    Log.d("CLOsToTasks", "Loading mappings for typeId: $typeId, offeredCourseId: $ocId")
//                    val response = RetrofitInstance.apiService.getQuestionCloMappingsByActivityType(ocId, typeId)
//                    if (response.isSuccessful) {
//                        response.body()?.let { mappings ->
//                            currentMappings = mappings
//                            // Check if the data list is empty
//                            if (mappings.data.isEmpty()) {
//                                mappingTable.removeAllViews() // Clear any previous table content
//                                // Add a centered message to the table layout
//                                addCenteredMessageToTable(
//                                    if (typeId == 1) "This activity type (typeId 1) does not have direct CLO mappings."
//                                    else "No CLO mappings found for the selected activity type."
//                                )
//                                // Hide edit button if no data to edit
//                                editButton.visibility = View.GONE
//                                return@launch
//                            } else {
//                                // If data exists, show the table
//                                editButton.visibility = View.VISIBLE // Show edit button
//                                if (isEditMode) {
//                                    showEditableQuestionMappings(mappings)
//                                } else {
//                                    showQuestionMappings(mappings)
//                                }
//                            }
//                        }
//                    } else {
//                        val errorBody = response.errorBody()?.string() ?: "No error body"
//                        Log.e("CLOsToTasks", "Failed to load question mappings: $errorBody")
//                        showError("No CLO mappings found for the selected activity type.")
//                    }
//                } catch (e: Exception) {
//                    Log.e("CLOsToTasks", "Network error", e)
//                    showError("Network error: ${e.message}. Please check your connection.")
//                }
//            }
//        }
//    }
//
//    private fun onEditClicked() {
//        isEditMode = !isEditMode
//        currentMappings?.let { mappings ->
//            if (isEditMode) {
//                showEditableQuestionMappings(mappings)
//                editButton.text = "Save"
//                // Disable activity type buttons when in edit mode
//                setActivityButtonsEnabled(false)
//            } else {
//                // When "Save" is clicked, first attempt to save all changes
//                saveAllChanges()
//                // Re-enable activity type buttons when out of edit mode
//                setActivityButtonsEnabled(true)
//            }
//        }
//    }
//
//    private fun setActivityButtonsEnabled(enabled: Boolean) {
//        for (i in 0 until buttonsContainer.childCount) {
//            val row = buttonsContainer.getChildAt(i) as LinearLayout
//            for (j in 0 until row.childCount) {
//                val button = row.getChildAt(j) as Button
//                button.isEnabled = enabled
//                // Optionally adjust visual state for disabled buttons
//                button.alpha = if (enabled) 1.0f else 0.5f
//            }
//        }
//    }
//
//    private fun showQuestionMappings(response: QuestionCloMappingsResponse) {
//        activity?.runOnUiThread {
//            mappingTable.removeAllViews()
//
//            if (response.data.isEmpty()) {
//                // This case should ideally be caught by loadQuestionMappings already,
//                // but as a safeguard, if data is empty, clear table and show message.
//                addCenteredMessageToTable("No CLO mappings found for this activity type.")
//                return@runOnUiThread
//            }
//
//            val uniqueCLOs = response.data.flatMap { task ->
//                task.Questions.flatMap { question ->
//                    question.CLOs.map { it.CLO_ID }
//                }
//            }.distinct().sorted()
//
//            createTableHeader(uniqueCLOs)
//
//            for (task in response.data) {
//                val headerText = task.SmallTaskId ?: task.TaskName
//                addTaskHeaderRow(headerText, uniqueCLOs.size)
//
//                for (question in task.Questions) {
//                    addQuestionRow(question, uniqueCLOs)
//                }
//            }
//            // Add the totals row after all task rows
//            calculateAndAddColumnTotals(response.data, uniqueCLOs)
//        }
//    }
//
//    private fun showEditableQuestionMappings(response: QuestionCloMappingsResponse) {
//        activity?.runOnUiThread {
//            mappingTable.removeAllViews()
//
//            if (response.data.isEmpty()) {
//                // This case should ideally be caught by loadQuestionMappings already.
//                addCenteredMessageToTable("No CLO mappings found for this activity type to edit.")
//                return@runOnUiThread
//            }
//
//            val uniqueCLOs = response.data.flatMap { task ->
//                task.Questions.flatMap { question ->
//                    question.CLOs.map { it.CLO_ID }
//                }
//            }.distinct().sorted()
//
//            createTableHeader(uniqueCLOs)
//
//            for (task in response.data) {
//                val headerText = task.SmallTaskId ?: task.TaskName
//                addTaskHeaderRow(headerText, uniqueCLOs.size)
//
//                for (question in task.Questions) {
//                    addEditableQuestionRow(question, uniqueCLOs, task.TaskId)
//                }
//            }
//            // Add the totals row after all task rows
//            calculateAndAddColumnTotals(response.data, uniqueCLOs)
//        }
//    }
//
//    private fun createTableHeader(uniqueCLOs: List<Int>) {
//        val headerRow = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundResource(R.color.table_header_bg) // Apply background to the whole row
//        }
//
//        // Question header cell - fixed width
//        headerRow.addView(TextView(context).apply {
//            text = "Question"
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            setTypeface(null, Typeface.BOLD)
//            textSize = HEADER_TEXT_SIZE_SP
//            gravity = Gravity.CENTER
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(80), // Fixed width for Question column
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        // CLO header cell - fixed width for the "CLO" label
//        headerRow.addView(TextView(context).apply {
//            text = "CLO"
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            setTypeface(null, Typeface.BOLD)
//            textSize = HEADER_TEXT_SIZE_SP
//            gravity = Gravity.CENTER
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(40), // Fixed width for CLO label
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        // Dynamic CLO ID header cells - use weight for distribution
//        for (cloId in uniqueCLOs) {
//            headerRow.addView(TextView(context).apply {
//                text = "$cloId"
//                setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//                setTypeface(null, Typeface.BOLD)
//                textSize = HEADER_TEXT_SIZE_SP
//                gravity = Gravity.CENTER
//                layoutParams = TableRow.LayoutParams(
//                    0, // 0 width, distribute based on weight
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    1f // Assign weight to distribute space
//                )
//            })
//        }
//
//        mappingTable.addView(headerRow)
//    }
//
//    private fun addTaskHeaderRow(headerText: String, cloColumns: Int) {
//        val taskRow = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundResource(R.color.task_header_bg)
//        }
//
//        val taskCell = TextView(context).apply {
//            text = headerText.replace("_", " ")
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            setTypeface(null, Typeface.BOLD)
//            textSize = CELL_TEXT_SIZE_SP + 2f // Slightly larger for task titles
//            gravity = Gravity.START // Align to start for readability
//        }
//
//        val params = TableRow.LayoutParams().apply {
//            span = cloColumns + 2 // Span across Question column, "CLO" label column, and all dynamic CLO ID columns
//            width = TableRow.LayoutParams.MATCH_PARENT
//            height = TableRow.LayoutParams.WRAP_CONTENT
//        }
//        taskRow.addView(taskCell, params)
//        mappingTable.addView(taskRow)
//    }
//
//    private fun addQuestionRow(question: QuestionMapping, uniqueCLOs: List<Int>) {
//        val row = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundResource(R.color.table_cell_bg)
//        }
//
//        // Question number cell - fixed width
//        row.addView(TextView(context).apply {
//            text = question.QuestionNo.replace("_", " ")
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            textSize = CELL_TEXT_SIZE_SP
//            gravity = Gravity.CENTER
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(80), // Match Question header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        // Empty cell for "CLO" label column - fixed width
//        row.addView(TextView(context).apply {
//            text = "" // Empty cell
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(40), // Match CLO label header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        val cloMap = question.CLOs.associateBy { it.CLO_ID }
//        for (cloId in uniqueCLOs) {
//            val percentage = cloMap[cloId]?.Percentage ?: "-"
//            row.addView(TextView(context).apply {
//                text = percentage
//                setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//                textSize = CELL_TEXT_SIZE_SP
//                gravity = Gravity.CENTER
//                layoutParams = TableRow.LayoutParams(
//                    0, // 0 width, distribute based on weight
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    1f // Assign weight
//                )
//            })
//        }
//
//        mappingTable.addView(row)
//    }
//
//    private fun addEditableQuestionRow(
//        question: QuestionMapping,
//        uniqueCLOs: List<Int>,
//        taskId: Int
//    ) {
//        val row = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundResource(R.color.table_cell_editable_bg)
//        }
//
//        // Question number cell - fixed width
//        row.addView(TextView(context).apply {
//            text = question.QuestionNo.replace("_", " ")
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            textSize = CELL_TEXT_SIZE_SP
//            gravity = Gravity.CENTER
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(80), // Match Question header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        // Empty cell for "CLO" label column - fixed width
//        row.addView(TextView(context).apply {
//            text = "" // Empty cell
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(40), // Match CLO label header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        val cloMap = question.CLOs.associateBy { it.CLO_ID }
//        for (cloId in uniqueCLOs) {
//            val percentage = cloMap[cloId]?.Percentage ?: "" // Use empty string for editable
//            val editText = EditText(context).apply {
//                setText(percentage)
//                setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//                setBackgroundResource(R.color.table_cell_editable_bg) // Use background for editable cells
//                gravity = Gravity.CENTER
//                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
//                textSize = CELL_TEXT_SIZE_SP
//                tag = Pair(question.QuestionId, cloId) // Store QuestionId and CLO_ID as tag
//
//                layoutParams = TableRow.LayoutParams(
//                    0, // 0 width, distribute based on weight
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    1f // Assign weight
//                )
//            }
//            row.addView(editText)
//        }
//
//        mappingTable.addView(row)
//    }
//
//    private fun calculateAndAddColumnTotals(data: List<TaskMapping>, uniqueCLOs: List<Int>) {
//        val columnTotals = mutableMapOf<Int, Int>()
//        uniqueCLOs.forEach { cloId ->
//            columnTotals[cloId] = 0
//        }
//
//        data.forEach { task ->
//            task.Questions.forEach { question ->
//                question.CLOs.forEach { clo ->
//                    if (uniqueCLOs.contains(clo.CLO_ID)) {
//                        columnTotals[clo.CLO_ID] = (columnTotals[clo.CLO_ID] ?: 0) + (clo.Percentage.toIntOrNull() ?: 0)
//                    }
//                }
//            }
//        }
//
//        addTotalsRow(columnTotals, uniqueCLOs)
//    }
//
//    private fun addTotalsRow(columnTotals: Map<Int, Int>, uniqueCLOs: List<Int>) {
//        val totalsRow = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            setBackgroundResource(R.color.table_footer_bg) // Apply background to the whole row
//        }
//
//        // "Total" label cell - fixed width
//        totalsRow.addView(TextView(context).apply {
//            text = "Total"
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            setTypeface(null, Typeface.BOLD)
//            textSize = HEADER_TEXT_SIZE_SP
//            setTextColor(Color.BLACK) // Make total label black for contrast
//            gravity = Gravity.CENTER
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(80), // Match Question header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        // Empty cell next to "Total" (for alignment with the CLO label column) - fixed width
//        totalsRow.addView(TextView(context).apply {
//            text = ""
//            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//            layoutParams = TableRow.LayoutParams(
//                dpToPx(40), // Match CLO label header width
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//        })
//
//        for (cloId in uniqueCLOs) {
//            val total = columnTotals[cloId] ?: 0
//            totalsRow.addView(TextView(context).apply {
//                text = "$total%"
//                setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
//                setTypeface(null, Typeface.BOLD)
//                textSize = CELL_TEXT_SIZE_SP // Keep consistent with data cells
//                setTextColor(Color.WHITE) // White text for total cells
//                setBackgroundResource(
//                    if (total == 100) R.color.green_background else R.color.red_background
//                )
//                gravity = Gravity.CENTER
//                layoutParams = TableRow.LayoutParams(
//                    0, // 0 width, distribute based on weight
//                    TableRow.LayoutParams.WRAP_CONTENT,
//                    1f // Assign weight
//                )
//            })
//        }
//        mappingTable.addView(totalsRow)
//    }
//
//    // Helper function to convert DP to Pixels
//    private fun dpToPx(dp: Int): Int {
//        return (dp * resources.displayMetrics.density).toInt()
//    }
//
//    /**
//     * Collects all entered percentages from the editable table, validates them,
//     * and sends them to the API.
//     */
//    private fun saveAllChanges() {
//        val updatesToSend = mutableListOf<UpdatePercentageRequest>()
//        val currentColumnTotals = mutableMapOf<Int, Double>() // Using Double for more precision
//
//        // Initialize current totals based on the original data (before any local edits)
//        currentMappings?.data?.forEach { task ->
//            task.Questions.forEach { question ->
//                question.CLOs.forEach { clo ->
//                    currentColumnTotals[clo.CLO_ID] = (currentColumnTotals[clo.CLO_ID] ?: 0.0) + (clo.Percentage.toDoubleOrNull() ?: 0.0)
//                }
//            }
//        }
//
//
//        // Iterate through the table rows to get updated values from EditTexts
//        // Start from the third row (index 2) to skip header and CLO label rows.
//        // Also skip task header rows and total row.
//        for (i in 0 until mappingTable.childCount) {
//            val row = mappingTable.getChildAt(i)
//            if (row is TableRow) {
//                // Only process rows that are editable question rows
//                if (row.background.constantState == ContextCompat.getDrawable(requireContext(), R.color.table_cell_editable_bg)?.constantState) {
//                    for (j in 2 until row.childCount) { // Start from index 2 to skip QuestionNo TextView and CLO Empty TextView
//                        val cellView = row.getChildAt(j)
//                        if (cellView is EditText) {
//                            val (questionId, cloId) = cellView.tag as Pair<Int, Int>
//                            val newPercentageStr = cellView.text.toString()
//
//                            // --- Percentage Format Validation ---
//                            if (!newPercentageStr.matches(Regex("^100(\\.0{1,2})?$|^\\d{1,2}(\\.\\d{1,2})?$"))) {
//                                showError("Invalid percentage format for CLO $cloId. Must be 0-100 with max 2 decimals.")
//                                return // Stop saving process if any format is invalid
//                            }
//
//                            val newPercentage = newPercentageStr.toDoubleOrNull() ?: 0.0
//
//                            // Get the old percentage for this specific mapping to calculate the delta
//                            val oldPercentageForMapping = currentMappings?.data
//                                ?.flatMap { it.Questions }
//                                ?.firstOrNull { it.QuestionId == questionId }
//                                ?.CLOs
//                                ?.firstOrNull { it.CLO_ID == cloId }
//                                ?.Percentage?.toDoubleOrNull() ?: 0.0
//
//                            // Calculate the adjusted total for this CLO
//                            // Remove old percentage from total and add new one
//                            val adjustedTotal = (currentColumnTotals[cloId] ?: 0.0) - oldPercentageForMapping + newPercentage
//
//                            // --- 100% Exceedance Validation ---
//                            if (adjustedTotal > 100.0) {
//                                showError("Mapping for CLO $cloId exceeds 100%. Current total: ${adjustedTotal.toInt()}%")
//                                return // Stop saving process if any CLO total exceeds 100%
//                            }
//
//                            // If valid, add to list of updates
//                            updatesToSend.add(
//                                UpdatePercentageRequest(
//                                    questionId = questionId,
//                                    cloId = cloId,
//                                    newPercentage = newPercentageStr
//                                )
//                            )
//
//                            // Update the running total for this CLO for subsequent validations within this loop
//                            currentColumnTotals[cloId] = adjustedTotal
//                        }
//                    }
//                }
//            }
//        }
//
//        // If all validations pass, proceed with sending updates
//        if (updatesToSend.isNotEmpty()) {
//            lifecycleScope.launch {
//                var allSuccess = true
//                for (updateRequest in updatesToSend) {
//                    try {
//                        val response = RetrofitInstance.apiService.updateQuestionCloMappingPercentage(updateRequest)
//                        if (!response.isSuccessful) {
//                            val error = response.errorBody()?.string() ?: "Unknown error"
//                            showError("Failed to update Q${updateRequest.questionId} CLO${updateRequest.cloId}: $error")
//                            allSuccess = false
//                            // Potentially stop here or continue trying to update others
//                        }
//                    } catch (e: Exception) {
//                        showError("Network error updating Q${updateRequest.questionId} CLO${updateRequest.cloId}: ${e.message}")
//                        Log.e("CLOsToTasks", "Update error", e)
//                        allSuccess = false
//                        // Potentially stop here or continue trying to update others
//                    }
//                }
//
//                if (allSuccess) {
//                    showSuccess("All percentages updated successfully!")
//                } else {
//                    showError("Some updates failed. Check logs for details.")
//                }
//                // Refresh data regardless of full success to reflect any changes that went through
//                currentTypeId?.let { loadQuestionMappings(it) }
//                isEditMode = false // Exit edit mode
//                editButton.text = "Edit" // Change button text back
//            }
//        } else {
//            showInfo("No changes to save.") // Changed to showInfo
//            isEditMode = false // Exit edit mode
//            editButton.text = "Edit" // Change button text back
//        }
//    }
//
//
//    private fun showSuccess(message: String) {
//        activity?.runOnUiThread {
//            Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
//                setGravity(Gravity.CENTER, 0, 0)
//                show()
//            }
//        }
//    }
//
//    private fun showError(message: String) {
//        activity?.runOnUiThread {
//            Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
//                setGravity(Gravity.CENTER, 0, 0)
//                show()
//            }
//        }
//    }
//
//    // New helper function for informative messages (not errors)
//    private fun showInfo(message: String, duration: Int = Toast.LENGTH_SHORT) {
//        activity?.runOnUiThread {
//            Toast.makeText(context, message, duration).apply {
//                setGravity(Gravity.CENTER, 0, 0)
//                show()
//            }
//        }
//    }
//
//    // New helper function to display messages within the table area
//    private fun addCenteredMessageToTable(message: String) {
//        val row = TableRow(context).apply {
//            layoutParams = TableLayout.LayoutParams(
//                TableLayout.LayoutParams.MATCH_PARENT,
//                TableLayout.LayoutParams.WRAP_CONTENT
//            )
//            gravity = Gravity.CENTER // Center the row content
//        }
//
//        val textView = TextView(context).apply {
//            text = message
//            textSize = 16f
//            setTextColor(ContextCompat.getColor(context, R.color.text_dark)) // Use a subtle color (ensure R.color.text_color_secondary exists in your colors.xml)
//            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
//            gravity = Gravity.CENTER
//        }
//
//        val params = TableRow.LayoutParams().apply {
//            span = 10 // Span across enough columns to center it (adjust as needed based on max possible columns, 10 is a safe bet)
//            width = TableRow.LayoutParams.MATCH_PARENT
//            height = TableRow.LayoutParams.WRAP_CONTENT
//        }
//        row.addView(textView, params)
//        mappingTable.addView(row)
//    }
//
//    companion object {
//        fun newInstance(
//            courseCode: String,
//            courseName: String,
//            section: String,
//            semester: Int,
//            offeredCourseId: Int
//        ): CLOsToTasksFragment {
//            return CLOsToTasksFragment().apply {
//                arguments = Bundle().apply {
//                    putString("courseCode", courseCode)
//                    putString("courseName", courseName)
//                    putString("section", section)
//                    putInt("semester", semester)
//                    putInt("offeredCourseId", offeredCourseId)
//                }
//            }
//        }
//    }
//}

package com.example.obe_mngt_sys.ACTIVITIES

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.ActivityType
import com.example.obe_mngt_sys.MODELS.QuestionCloMappingsResponse
import com.example.obe_mngt_sys.MODELS.UpdatePercentageRequest
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.launch

class CLOsToTasksFragment : Fragment() {

    data class TaskMapping(
        val TaskId: Int,
        val TaskName: String,
        val ActivityType: String,
        val SmallTaskId: String?,
        val Questions: List<QuestionMapping>
    )

    data class QuestionMapping(
        val QuestionNo: String,
        val QuestionId: Int,
        val CLOs: List<CloMapping>
    )

    data class CloMapping(
        val CLO_ID: Int,
        val Percentage: String
    )

    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var teacherId: String = ""
    private var semester: Int? = null
    private var offeredCourseId: Int? = null
    private lateinit var buttonsContainer: LinearLayout
    private lateinit var mappingTable: TableLayout
    private lateinit var editButton: Button
    private var isEditMode = false
    private var currentMappings: QuestionCloMappingsResponse? = null
    private var currentTypeId: Int? = null

    // Define standard padding and text sizes
    private val CELL_PADDING_DP = 8
    private val HEADER_TEXT_SIZE_SP = 14f
    private val CELL_TEXT_SIZE_SP = 12f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString("courseCode")
            courseName = it.getString("courseName")
            section = it.getString("section")
            semester = it.getInt("semester")
            offeredCourseId = it.getInt("offeredCourseId")
            teacherId = it.getString("teacherId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_c_l_os_to_tasks, container, false)
        buttonsContainer = view.findViewById(R.id.buttons_container)
        mappingTable = view.findViewById(R.id.mapping_table)
        editButton = view.findViewById(R.id.editButton)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val courseName = arguments?.getString("courseName") ?: "N/A"
        val section = arguments?.getString("section") ?: "N/A"
        val semester = arguments?.getInt("semester") ?: 0

        view.findViewById<TextView>(R.id.textViewCourseName).text = "Course: $courseName"
        view.findViewById<TextView>(R.id.textViewSection).text = "Section: $section"
        view.findViewById<TextView>(R.id.textViewSemester).text = "Semester: $semester"

        editButton.setOnClickListener { onEditClicked() }
        loadActivities()
    }

    private fun loadActivities() {
        offeredCourseId?.let { ocId ->
            teacherId?.let { tId ->
                lifecycleScope.launch {
                    try {
                        val response = RetrofitInstance.apiService.GetActivitiesbtn(ocId, tId)
                        if (response.isSuccessful) {
                            response.body()?.let { activities ->
                                createActivityButtons(activities)
                            }
                        } else {
                            showError("Failed to load activities")
                        }
                    } catch (e: Exception) {
                        showError("Network error: ${e.message}")
                    }
                }
            } ?: showError("Teacher ID not available")
        } ?: showError("Course ID not available")
    }

    private fun createActivityButtons(activities: List<ActivityType>) {
        activity?.runOnUiThread {
            buttonsContainer.removeAllViews()

            if (activities.isEmpty()) {
                buttonsContainer.addView(TextView(requireContext()).apply {
                    text = "No activities available"
                    gravity = Gravity.CENTER
                    setPadding(0, 16.dpToPx(), 0, 0)
                })
                return@runOnUiThread
            }

            val buttonParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                val marginInPx = 4.dpToPx()
                setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            }

            var currentRow: LinearLayout? = null

            activities.forEachIndexed { index, activity ->
                if (index % 3 == 0) {
                    currentRow = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        buttonsContainer.addView(this)
                    }
                }

                val button = Button(requireContext()).apply {
                    text = activity.Typee
                    layoutParams = buttonParams
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#469FD1"))
                    setTextColor(Color.WHITE)
                    textSize = 12f
                    setPadding(12.dpToPx(), 4.dpToPx(), 12.dpToPx(), 4.dpToPx())
                    minWidth = 64.dpToPx()
                    minHeight = 36.dpToPx()

                    setOnClickListener {
                        resetButtonColors()
                        this.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2c7be5"))
                        currentTypeId = activity.typeId
                        loadQuestionMappings(activity.typeId)
                    }
                }

                currentRow?.addView(button)
            }
        }
    }

    private fun resetButtonColors() {
        for (i in 0 until buttonsContainer.childCount) {
            val row = buttonsContainer.getChildAt(i) as? LinearLayout ?: continue
            for (j in 0 until row.childCount) {
                val button = row.getChildAt(j) as? Button ?: continue
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#469FD1"))
            }
        }
    }

    // Extension function for dp to px conversion
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun loadQuestionMappings(typeId: Int) {
        offeredCourseId?.let { ocId ->
            lifecycleScope.launch {
                try {
                    Log.d("CLOsToTasks", "Loading mappings for typeId: $typeId, offeredCourseId: $ocId")
                    val response = RetrofitInstance.apiService.getQuestionCloMappingsByActivityType(ocId, typeId)
                    if (response.isSuccessful) {
                        response.body()?.let { mappings ->
                            currentMappings = mappings
                            if (mappings.data.isEmpty()) {
                                mappingTable.removeAllViews()
                                addCenteredMessageToTable(
                                    if (typeId == 1) "This activity type (typeId 1) does not have direct CLO mappings."
                                    else "No CLO mappings found for the selected activity type."
                                )
                                editButton.visibility = View.GONE
                                return@launch
                            } else {
                                editButton.visibility = View.VISIBLE
                                if (isEditMode) {
                                    showEditableQuestionMappings(mappings)
                                } else {
                                    showQuestionMappings(mappings)
                                }
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "No error body"
                        Log.e("CLOsToTasks", "Failed to load question mappings: $errorBody")
                        showError("No CLO mappings found for the selected activity type.")
                    }
                } catch (e: Exception) {
                    Log.e("CLOsToTasks", "Network error", e)
                    showError("Network error: ${e.message}. Please check your connection.")
                }
            }
        }
    }

    private fun onEditClicked() {
        isEditMode = !isEditMode
        currentMappings?.let { mappings ->
            if (isEditMode) {
                showEditableQuestionMappings(mappings)
                editButton.text = "Save"
                setActivityButtonsEnabled(false)
            } else {
                saveAllChanges()
                setActivityButtonsEnabled(true)
            }
        }
    }

    private fun setActivityButtonsEnabled(enabled: Boolean) {
        for (i in 0 until buttonsContainer.childCount) {
            val row = buttonsContainer.getChildAt(i) as LinearLayout
            for (j in 0 until row.childCount) {
                val button = row.getChildAt(j) as Button
                button.isEnabled = enabled
                button.alpha = if (enabled) 1.0f else 0.5f
            }
        }
    }

    private fun showQuestionMappings(response: QuestionCloMappingsResponse) {
        activity?.runOnUiThread {
            mappingTable.removeAllViews()

            if (response.data.isEmpty()) {
                addCenteredMessageToTable("No CLO mappings found for this activity type.")
                return@runOnUiThread
            }

            val uniqueCLOs = response.data.flatMap { task ->
                task.Questions.flatMap { question ->
                    question.CLOs.map { it.CLO_ID }
                }
            }.distinct().sorted()

            createTableHeader(uniqueCLOs)

            for (task in response.data) {
                val headerText = task.SmallTaskId ?: task.TaskName
                addTaskHeaderRow(headerText, uniqueCLOs.size)

                for (question in task.Questions) {
                    addQuestionRow(question, uniqueCLOs)
                }
            }
            calculateAndAddColumnTotals(response.data, uniqueCLOs)
        }
    }

    private fun showEditableQuestionMappings(response: QuestionCloMappingsResponse) {
        activity?.runOnUiThread {
            mappingTable.removeAllViews()

            if (response.data.isEmpty()) {
                addCenteredMessageToTable("No CLO mappings found for this activity type to edit.")
                return@runOnUiThread
            }

            val uniqueCLOs = response.data.flatMap { task ->
                task.Questions.flatMap { question ->
                    question.CLOs.map { it.CLO_ID }
                }
            }.distinct().sorted()

            createTableHeader(uniqueCLOs)

            for (task in response.data) {
                val headerText = task.SmallTaskId ?: task.TaskName
                addTaskHeaderRow(headerText, uniqueCLOs.size)

                for (question in task.Questions) {
                    addEditableQuestionRow(question, uniqueCLOs, task.TaskId)
                }
            }
            calculateAndAddColumnTotals(response.data, uniqueCLOs)
        }
    }

    private fun createTableHeader(uniqueCLOs: List<Int>) {
        val headerRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.table_header_bg)
        }

        headerRow.addView(TextView(context).apply {
            text = "Question"
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            setTypeface(null, Typeface.BOLD)
            textSize = HEADER_TEXT_SIZE_SP
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                80.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        headerRow.addView(TextView(context).apply {
            text = "CLO"
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            setTypeface(null, Typeface.BOLD)
            textSize = HEADER_TEXT_SIZE_SP
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                40.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        for (cloId in uniqueCLOs) {
            headerRow.addView(TextView(context).apply {
                text = "$cloId"
                setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
                setTypeface(null, Typeface.BOLD)
                textSize = HEADER_TEXT_SIZE_SP
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }

        mappingTable.addView(headerRow)
    }

    private fun addTaskHeaderRow(headerText: String, cloColumns: Int) {
        val taskRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.task_header_bg)
        }

        val taskCell = TextView(context).apply {
            text = headerText.replace("_", " ")
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            setTypeface(null, Typeface.BOLD)
            textSize = CELL_TEXT_SIZE_SP + 2f
            gravity = Gravity.START
        }

        val params = TableRow.LayoutParams().apply {
            span = cloColumns + 2
            width = TableRow.LayoutParams.MATCH_PARENT
            height = TableRow.LayoutParams.WRAP_CONTENT
        }
        taskRow.addView(taskCell, params)
        mappingTable.addView(taskRow)
    }

    private fun addQuestionRow(question: QuestionMapping, uniqueCLOs: List<Int>) {
        val row = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.table_cell_bg)
        }

        row.addView(TextView(context).apply {
            text = question.QuestionNo.replace("_", " ")
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            textSize = CELL_TEXT_SIZE_SP
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                80.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        row.addView(TextView(context).apply {
            text = ""
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            layoutParams = TableRow.LayoutParams(
                40.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        val cloMap = question.CLOs.associateBy { it.CLO_ID }
        for (cloId in uniqueCLOs) {
            val percentage = cloMap[cloId]?.Percentage ?: "-"
            row.addView(TextView(context).apply {
                text = percentage
                setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
                textSize = CELL_TEXT_SIZE_SP
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }

        mappingTable.addView(row)
    }

    private fun addEditableQuestionRow(
        question: QuestionMapping,
        uniqueCLOs: List<Int>,
        taskId: Int
    ) {
        val row = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.table_cell_editable_bg)
        }

        row.addView(TextView(context).apply {
            text = question.QuestionNo.replace("_", " ")
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            textSize = CELL_TEXT_SIZE_SP
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                80.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        row.addView(TextView(context).apply {
            text = ""
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            layoutParams = TableRow.LayoutParams(
                40.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        val cloMap = question.CLOs.associateBy { it.CLO_ID }
        for (cloId in uniqueCLOs) {
            val percentage = cloMap[cloId]?.Percentage ?: ""
            val editText = EditText(context).apply {
                setText(percentage)
                setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
                setBackgroundResource(R.color.table_cell_editable_bg)
                gravity = Gravity.CENTER
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                textSize = CELL_TEXT_SIZE_SP
                tag = Pair(question.QuestionId, cloId)
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            row.addView(editText)
        }

        mappingTable.addView(row)
    }

    private fun calculateAndAddColumnTotals(data: List<TaskMapping>, uniqueCLOs: List<Int>) {
        val columnTotals = mutableMapOf<Int, Int>()
        uniqueCLOs.forEach { cloId ->
            columnTotals[cloId] = 0
        }

        data.forEach { task ->
            task.Questions.forEach { question ->
                question.CLOs.forEach { clo ->
                    if (uniqueCLOs.contains(clo.CLO_ID)) {
                        columnTotals[clo.CLO_ID] = (columnTotals[clo.CLO_ID] ?: 0) + (clo.Percentage.toIntOrNull() ?: 0)
                    }
                }
            }
        }

        addTotalsRow(columnTotals, uniqueCLOs)
    }

    private fun addTotalsRow(columnTotals: Map<Int, Int>, uniqueCLOs: List<Int>) {
        val totalsRow = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.color.table_footer_bg)
        }

        totalsRow.addView(TextView(context).apply {
            text = "Total"
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            setTypeface(null, Typeface.BOLD)
            textSize = HEADER_TEXT_SIZE_SP
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(
                80.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        totalsRow.addView(TextView(context).apply {
            text = ""
            setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
            layoutParams = TableRow.LayoutParams(
                40.dpToPx(),
                TableRow.LayoutParams.WRAP_CONTENT
            )
        })

        for (cloId in uniqueCLOs) {
            val total = columnTotals[cloId] ?: 0
            totalsRow.addView(TextView(context).apply {
                text = "$total%"
                setPadding(CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx(), CELL_PADDING_DP.dpToPx())
                setTypeface(null, Typeface.BOLD)
                textSize = CELL_TEXT_SIZE_SP
                setTextColor(Color.WHITE)
                setBackgroundResource(
                    if (total == 100) R.color.green_background else R.color.red_background
                )
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
            })
        }
        mappingTable.addView(totalsRow)
    }

    private fun saveAllChanges() {
        val updatesToSend = mutableListOf<UpdatePercentageRequest>()
        val currentColumnTotals = mutableMapOf<Int, Double>()

        currentMappings?.data?.forEach { task ->
            task.Questions.forEach { question ->
                question.CLOs.forEach { clo ->
                    currentColumnTotals[clo.CLO_ID] = (currentColumnTotals[clo.CLO_ID] ?: 0.0) + (clo.Percentage.toDoubleOrNull() ?: 0.0)
                }
            }
        }

        for (i in 0 until mappingTable.childCount) {
            val row = mappingTable.getChildAt(i)
            if (row is TableRow) {
                if (row.background.constantState == ContextCompat.getDrawable(requireContext(), R.color.table_cell_editable_bg)?.constantState) {
                    for (j in 2 until row.childCount) {
                        val cellView = row.getChildAt(j)
                        if (cellView is EditText) {
                            val (questionId, cloId) = cellView.tag as Pair<Int, Int>
                            val newPercentageStr = cellView.text.toString()

                            if (!newPercentageStr.matches(Regex("^100(\\.0{1,2})?$|^\\d{1,2}(\\.\\d{1,2})?$"))) {
                                showError("Invalid percentage format for CLO $cloId. Must be 0-100 with max 2 decimals.")
                                return
                            }

                            val newPercentage = newPercentageStr.toDoubleOrNull() ?: 0.0

                            val oldPercentageForMapping = currentMappings?.data
                                ?.flatMap { it.Questions }
                                ?.firstOrNull { it.QuestionId == questionId }
                                ?.CLOs
                                ?.firstOrNull { it.CLO_ID == cloId }
                                ?.Percentage?.toDoubleOrNull() ?: 0.0

                            val adjustedTotal = (currentColumnTotals[cloId] ?: 0.0) - oldPercentageForMapping + newPercentage

                            if (adjustedTotal > 100.0) {
                                showError("Mapping for CLO $cloId exceeds 100%. Current total: ${adjustedTotal.toInt()}%")
                                return
                            }

                            updatesToSend.add(
                                UpdatePercentageRequest(
                                    questionId = questionId,
                                    cloId = cloId,
                                    newPercentage = newPercentageStr
                                )
                            )

                            currentColumnTotals[cloId] = adjustedTotal
                        }
                    }
                }
            }
        }

        if (updatesToSend.isNotEmpty()) {
            lifecycleScope.launch {
                var allSuccess = true
                for (updateRequest in updatesToSend) {
                    try {
                        val response = RetrofitInstance.apiService.updateQuestionCloMappingPercentage(updateRequest)
                        if (!response.isSuccessful) {
                            val error = response.errorBody()?.string() ?: "Unknown error"
                            showError("Failed to update Q${updateRequest.questionId} CLO${updateRequest.cloId}: $error")
                            allSuccess = false
                        }
                    } catch (e: Exception) {
                        showError("Network error updating Q${updateRequest.questionId} CLO${updateRequest.cloId}: ${e.message}")
                        Log.e("CLOsToTasks", "Update error", e)
                        allSuccess = false
                    }
                }

                if (allSuccess) {
                    showSuccess("All percentages updated successfully!")
                } else {
                    showError("Some updates failed. Check logs for details.")
                }
                currentTypeId?.let { loadQuestionMappings(it) }
                isEditMode = false
                editButton.text = "Edit"
            }
        } else {
            showInfo("No changes to save.")
            isEditMode = false
            editButton.text = "Edit"
        }
    }

    private fun showSuccess(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }

    private fun showError(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }

    private fun showInfo(message: String, duration: Int = Toast.LENGTH_SHORT) {
        activity?.runOnUiThread {
            Toast.makeText(context, message, duration).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }

    private fun addCenteredMessageToTable(message: String) {
        val row = TableRow(context).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
        }

        val textView = TextView(context).apply {
            text = message
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.text_dark))
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
            gravity = Gravity.CENTER
        }

        val params = TableRow.LayoutParams().apply {
            span = 10
            width = TableRow.LayoutParams.MATCH_PARENT
            height = TableRow.LayoutParams.WRAP_CONTENT
        }
        row.addView(textView, params)
        mappingTable.addView(row)
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            teacherId: String
        ): CLOsToTasksFragment {
            return CLOsToTasksFragment().apply {
                arguments = Bundle().apply {
                    putString("courseCode", courseCode)
                    putString("courseName", courseName)
                    putString("section", section)
                    putInt("semester", semester)
                    putInt("offeredCourseId", offeredCourseId)
                    putString("teacherId", teacherId)
                }
            }
        }
    }
}
