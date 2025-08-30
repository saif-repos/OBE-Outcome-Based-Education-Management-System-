package com.example.obe_mngt_sys.ACTIVITIES

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.CLOPLOMappingItem
import com.example.obe_mngt_sys.MODELS.CLOInfo
import com.example.obe_mngt_sys.MODELS.PermissionResponse
import com.example.obe_mngt_sys.R
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CLOToPLOFragment : Fragment() {

    private var tableHeader: TableLayout? = null
    private var tableLayout: TableLayout? = null
    private var offeredCourseId: Int = -1
    private var hasMappingPermission = false
    private var teacherId: String = ""
    private var isEditMode = false
    private var originalData: List<CLOPLOMappingItem> = emptyList()
    private val modifiedData = mutableListOf<CLOPLOMappingItem>()

    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnSubmit: Button
    private lateinit var textViewStatus: TextView

    // Define standard padding and text sizes
    private val CELL_PADDING_DP = 10
    private val HEADER_TEXT_SIZE_SP = 14f
    private val CELL_TEXT_SIZE_SP = 12f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offeredCourseId = it.getInt("offeredCourseId", -1)
            teacherId = it.getString("teacherId") ?: ""
            Log.d("CLOToPLOFragment", "Received teacherId: $teacherId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_c_l_o_to_p_l_o, container, false)

        tableHeader = view.findViewById(R.id.tableHeader)
        tableLayout = view.findViewById(R.id.tableLayout)
        textViewStatus = view.findViewById(R.id.textViewStatus)

        btnEdit = view.findViewById<Button>(R.id.btnEdit).also {
            it.visibility = View.GONE
        }
        btnSave = view.findViewById<Button>(R.id.btnSave).also {
            it.visibility = View.GONE
        }
        btnSubmit = view.findViewById<Button>(R.id.btnSubmit).also {
            it.visibility = View.GONE
        }

        // Set course info
        val courseName = arguments?.getString("courseName") ?: "N/A"
        val section = arguments?.getString("section") ?: "N/A"
        val semester = arguments?.getInt("semester") ?: 0

        view.findViewById<TextView>(R.id.textViewCourseName).text = "Course: $courseName"
        view.findViewById<TextView>(R.id.textViewSection).text = "Section: $section"
        view.findViewById<TextView>(R.id.textViewSemester).text = "Semester: $semester"

        // Check permission and load data
        if (offeredCourseId != -1 && teacherId.isNotEmpty()) {
            Log.d("CLOToPLOFragment", "Checking permission with teacherId: $teacherId")
            checkMappingPermission(offeredCourseId, teacherId)
            fetchMappingStatus(offeredCourseId)
        } else {
            Log.e("CLOToPLOFragment", "Missing required IDs - offeredCourseId: $offeredCourseId, teacherId: $teacherId")
            showError("Missing required information. Please go back and select a course.")
        }

        // Button click listeners
        btnEdit.setOnClickListener { enterEditMode() }
        btnSave.setOnClickListener { saveChanges() }
        btnSubmit.setOnClickListener { updateMappingStatus() }

        return view
    }

    private fun fetchMappingStatus(offeredCourseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetMappingStatus(offeredCourseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val status = response.body()?.status ?: "Unknown"
                        textViewStatus.text = "Status: $status"

                        when (status.lowercase()) {
                            "approved" -> textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_approved))
                            "pending" -> textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_pending))
                            "suggested" -> textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.status_suggested))
                            else -> textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        }
                    } else {
                        textViewStatus.text = "Status: Error loading"
                        textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textViewStatus.text = "Status: Network Error"
                    textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                    Log.e("CLOToPLOFragment", "Error fetching status: ${e.message}")
                }
            }
        }
    }

    private fun checkMappingPermission(offeredCourseId: Int, teacherId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.CheckPermission(offeredCourseId, teacherId)
                if (response.isSuccessful) {
                    hasMappingPermission = response.body()?.Result ?: false
                    withContext(Dispatchers.Main) {
                        btnEdit.visibility = if (hasMappingPermission) View.VISIBLE else View.GONE
                        btnSubmit.visibility = if (hasMappingPermission) View.VISIBLE else View.GONE
                        fetchCLOPLOMapping(offeredCourseId)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Permission check failed: ${response.code()}")
                        btnEdit.visibility = View.GONE
                        btnSubmit.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Permission check network error: ${e.message}")
                    btnEdit.visibility = View.GONE
                    btnSubmit.visibility = View.GONE
                }
            }
        }
    }

    private fun updateMappingStatus() {
        if (offeredCourseId == -1) {
            showToast("Invalid course ID", isError = true)
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."
        btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_disabled))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.updateMappingStatus(offeredCourseId)
                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_button_background))

                    if (response.isSuccessful) {
                        val result = response.body()
                        showToast(result?.message ?: "Status updated to pending", isError = false)
                        fetchMappingStatus(offeredCourseId)
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        showToast("Failed to update status: $errorBody", isError = true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit"
                    btnSubmit.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_button_background))
                    showToast("Network error: ${e.message}", isError = true)
                }
            }
        }
    }

    private fun fetchCLOPLOMapping(offeredCourseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getCLOPLOMapping(offeredCourseId)
                if (response.isSuccessful) {
                    val rawData = response.body() ?: emptyList()

                    originalData = rawData
                        .distinctBy { "${it.cloId}-${it.ploId}" }
                        .map { item ->
                            CLOPLOMappingItem(
                                cloId = item.cloId,
                                ploId = item.ploId,
                                percentage = item.percentage,
                                originalOcId = item.originalOcId,
                                isFromSameCourse = item.isFromSameCourse,
                                cloInfo = item.cloInfo ?: CLOInfo(
                                    id = item.cloId,
                                    description = "Unknown CLO",
                                    teacherId = "N/A"
                                )
                            )
                        }.sortedWith(compareBy({ it.cloId }, { it.ploId }))

                    withContext(Dispatchers.Main) {
                        populateGrid(originalData)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = response.errorBody()?.string()
                        showError("Failed to load mappings: ${errorBody ?: "Server error"}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Network error: ${e.message}. Please check your internet connection.")
                }
            }
        }
    }

    private fun populateGrid(data: List<CLOPLOMappingItem>) {
        tableHeader?.removeAllViews()
        tableLayout?.removeAllViews()

        if (data.isEmpty()) {
            showEmptyDataMessage()
            return
        }

        val groupedByClo = data.groupBy { it.cloId }
        val allPloIds = data.map { it.ploId }.distinct().sorted()

        // Create Table Header
        val headerRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_header_bg))
        }

        headerRow.addView(createHeaderCell("CLO", isFirstColumn = true))

        allPloIds.forEach { ploId ->
            headerRow.addView(createHeaderCell("PLO$ploId"))
        }

        if (hasMappingPermission && isEditMode) {
            headerRow.addView(createHeaderCell("Source", isSourceColumn = true))
        }
        tableHeader?.addView(headerRow)

        // Add rows for each CLO
        groupedByClo.forEach { (cloId, mappings) ->
            val row = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                setBackgroundResource(R.drawable.table_cell_background)
            }

            row.addView(createCloCell("CLO$cloId"))

            val ploMap = mappings.associateBy({ it.ploId }, { it })

            allPloIds.forEach { ploId ->
                val mapping = ploMap[ploId]
                val percentage = mapping?.percentage ?: 0f

                if (isEditMode && hasMappingPermission) {
                    row.addView(createEditableCell(cloId, ploId, percentage))
                } else {
                    row.addView(createDisplayCell(percentage))
                }
            }

            if (hasMappingPermission && isEditMode) {
                val isFromSameCourse = mappings.firstOrNull()?.isFromSameCourse ?: true
                val sourceText = if (isFromSameCourse) "Current" else "Shared"
                row.addView(createSourceCell(sourceText))
            }
            tableLayout?.addView(row)
        }

        if (!isEditMode) {
            val ploSumMap = mutableMapOf<Int, Float>()
            data.forEach { item ->
                ploSumMap[item.ploId] = (ploSumMap[item.ploId] ?: 0f) + item.percentage
            }

            val sumRow = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                setBackgroundResource(R.drawable.table_cell_background)
            }

            sumRow.addView(createHeaderCell("Total", isTotalLabel = true))

            allPloIds.forEach { ploId ->
                val sum = ploSumMap[ploId] ?: 0f
                sumRow.addView(createSumCell(sum))
            }
            tableLayout?.addView(sumRow)
        }
    }

    private fun createHeaderCell(text: String, isFirstColumn: Boolean = false, isSourceColumn: Boolean = false, isTotalLabel: Boolean = false): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = HEADER_TEXT_SIZE_SP
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.table_header_text))
            setBackgroundResource(R.drawable.table_header_cell_background)

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                when {
                    isFirstColumn -> 1.2f
                    isSourceColumn -> 0.8f
                    isTotalLabel -> 1.2f
                    else -> 1f
                }
            )
        }
    }

    private fun createCloCell(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = CELL_TEXT_SIZE_SP
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.table_cell_text))
            setBackgroundResource(R.drawable.table_cell_background)
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1.2f
            )
        }
    }

    private fun createDisplayCell(percentage: Float): TextView {
        return TextView(requireContext()).apply {
            text = if (percentage > 0f) "%.1f%%".format(percentage) else "-"
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = CELL_TEXT_SIZE_SP
            setTextColor(ContextCompat.getColor(requireContext(), R.color.table_cell_text))
            setBackgroundResource(R.drawable.table_cell_background)
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
    }

    private fun createEditableCell(cloId: Int, ploId: Int, percentage: Float): EditText {
        return EditText(requireContext()).apply {
            setText(if (percentage > 0f) "%.1f".format(percentage) else "")
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = CELL_TEXT_SIZE_SP
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            tag = "$cloId,$ploId"
            setBackgroundResource(R.drawable.table_editable_cell_background)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.table_editable_cell_text))

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val percentageStr = s.toString()
                    if (percentageStr.isNotEmpty()) {
                        val percentage = percentageStr.toFloatOrNull() ?: 0f
                        if (percentage > 100f) {
                            error = "Max 100%"
                        } else {
                            error = null
                        }
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
    }

    private fun createSourceCell(sourceText: String): TextView {
        return TextView(requireContext()).apply {
            text = sourceText
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = CELL_TEXT_SIZE_SP
            setTextColor(ContextCompat.getColor(requireContext(), R.color.table_cell_text))
            setBackgroundResource(R.drawable.table_cell_background)
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.8f
            )
        }
    }

    private fun createSumCell(sum: Float): TextView {
        return TextView(requireContext()).apply {
            text = "%.1f%%".format(sum)
            gravity = Gravity.CENTER
            setPadding(dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP), dpToPx(CELL_PADDING_DP))
            textSize = HEADER_TEXT_SIZE_SP
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setBackgroundResource(
                if (sum >= 50f) R.drawable.green_rounded_box
                else R.drawable.red_rounded_box
            )
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun validateInput(editText: EditText, percentageStr: String): Boolean {
        if (percentageStr.isEmpty()) {
            editText.error = null
            return true
        }

        val percentage = percentageStr.toFloatOrNull()
        return when {
            percentage == null -> {
                editText.error = "Invalid num"
                false
            }
            percentage < 0 -> {
                editText.error = "Min 0%"
                false
            }
            percentage > 100 -> {
                editText.error = "Max 100%"
                false
            }
            else -> {
                editText.error = null
                true
            }
        }
    }

    private fun validatePLOTotals(): Boolean {
        val ploTotalMap = mutableMapOf<Int, Float>()
        val invalidPLOs = mutableSetOf<Int>()

        modifiedData.forEach { item ->
            ploTotalMap[item.ploId] = (ploTotalMap[item.ploId] ?: 0f) + item.percentage
        }

        ploTotalMap.forEach { (ploId, total) ->
            if (total > 100f) {
                invalidPLOs.add(ploId)
            }
        }

        if (invalidPLOs.isNotEmpty()) {
            // Highlight the PLO columns that exceed 100%
            tableHeader?.let { header ->
                for (i in 1 until header.childCount) {
                    val headerCell = header.getChildAt(i) as? TextView
                    val headerText = headerCell?.text?.toString()?.removePrefix("PLO")?.toIntOrNull()

                    if (headerText != null && invalidPLOs.contains(headerText)) {
                        headerCell.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red_background))
                    } else {
                        headerCell?.setBackgroundResource(R.drawable.table_header_cell_background)
                    }
                }
            }

            val ploList = invalidPLOs.joinToString(", ") { "PLO$it" }
            showToast("The following PLOs exceed 100% total: $ploList", isError = true)
            return false
        }

        // Reset header background if no errors
        tableHeader?.let { header ->
            for (i in 0 until header.childCount) {
                header.getChildAt(i)?.setBackgroundResource(R.drawable.table_header_cell_background)
            }
        }

        return true
    }

    private fun enterEditMode() {
        isEditMode = true
        btnEdit.visibility = View.GONE
        btnSave.visibility = View.VISIBLE
        btnSubmit.visibility = View.GONE
        populateGrid(originalData)
    }

    private fun saveChanges() {
        modifiedData.clear()
        var hasErrors = false

        tableLayout?.let { table ->
            for (i in 0 until table.childCount) {
                val row = table.getChildAt(i) as? TableRow ?: continue
                val startIndex = 1
                val endIndex = row.childCount - (if (hasMappingPermission) 1 else 0)

                for (j in startIndex until endIndex) {
                    val editText = row.getChildAt(j) as? EditText
                    if (editText != null) {
                        val tag = editText.tag?.toString()
                        if (tag != null && tag.contains(",")) {
                            val (cloId, ploId) = tag.split(",").map { it.toInt() }
                            val percentageStr = editText.text.toString()

                            if (validateInput(editText, percentageStr)) {
                                val percentage = percentageStr.toFloatOrNull() ?: 0f
                                modifiedData.add(
                                    CLOPLOMappingItem(
                                        cloId = cloId,
                                        ploId = ploId,
                                        percentage = percentage,
                                        originalOcId = offeredCourseId,
                                        isFromSameCourse = true,
                                        cloInfo = null
                                    )
                                )
                            } else {
                                hasErrors = true
                            }
                        }
                    }
                }
            }
        }

        if (hasErrors) {
            showToast("Please fix validation errors before saving.", isError = true)
            return
        }

        // Validate PLO totals (should not exceed 100%)
        if (!validatePLOTotals()) {
            return
        }

        if (modifiedData.isEmpty()) {
            showToast("No changes detected to save.", isError = false)
            exitEditMode()
            return
        }

        btnSave.text = "Saving..."
        btnSave.isEnabled = false
        btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.button_disabled))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = mutableListOf<Boolean>()
                modifiedData.forEach { item ->
                    val response = RetrofitInstance.apiService.AddPloMapClo(
                        offeredCourseId,
                        item.cloId,
                        item.ploId,
                        item.percentage
                    )
                    results.add(response.isSuccessful)
                }

                withContext(Dispatchers.Main) {
                    btnSave.text = "Save"
                    btnSave.isEnabled = true
                    btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_button_background))

                    if (results.all { it }) {
                        showToast("Changes saved successfully!", isError = false)
                        fetchCLOPLOMapping(offeredCourseId)
                        fetchMappingStatus(offeredCourseId)
                        exitEditMode()
                    } else {
                        showToast("Some changes failed to save. Please try again.", isError = true)
                        fetchCLOPLOMapping(offeredCourseId)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSave.text = "Save"
                    btnSave.isEnabled = true
                    btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_button_background))
                    showToast("Error saving changes: ${e.message}", isError = true)
                }
            }
        }
    }

    private fun exitEditMode() {
        isEditMode = false
        btnEdit.visibility = if (hasMappingPermission) View.VISIBLE else View.GONE
        btnSave.visibility = View.GONE
        btnSubmit.visibility = if (hasMappingPermission) View.VISIBLE else View.GONE
        populateGrid(originalData)
    }

    private fun showEmptyDataMessage() {
        tableHeader?.removeAllViews()
        tableLayout?.let { table ->
            table.removeAllViews()
            val row = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                addView(TextView(requireContext()).apply {
                    text = "No CLO-PLO mapping data available for this course. Contact admin."
                    gravity = Gravity.CENTER
                    setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    ).apply { span = 5 }
                })
            }
            table.addView(row)
        }
    }

    private fun showError(message: String) {
        tableHeader?.removeAllViews()
        tableLayout?.let { table ->
            table.removeAllViews()
            val row = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                addView(TextView(requireContext()).apply {
                    text = "Error: $message"
                    gravity = Gravity.CENTER
                    setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    ).apply { span = 5 }
                })
            }
            table.addView(row)
        }
    }

    private fun showToast(message: String, isError: Boolean) {
        activity?.runOnUiThread {
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            val view = toast.view
            view?.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), if (isError) R.color.error_red else R.color.success_green)
            )
            val text = view?.findViewById<TextView>(android.R.id.message)
            text?.setTextColor(Color.WHITE)
            toast.show()
        }
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            teacherId: String
        ): CLOToPLOFragment {
            return CLOToPLOFragment().apply {
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