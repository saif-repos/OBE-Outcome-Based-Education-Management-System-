package com.example.obe_mngt_sys.ACTIVITIES

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
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
import com.example.obe_mngt_sys.MODELS.CloActivityMapping
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CLOsToActivitiesFragment : Fragment() {

    private var tableHeader: TableLayout? = null
    private var tableLayout: TableLayout? = null
    private var tableFooter: TableLayout? = null
    private var offeredCourseId: Int = -1
    private var isEditMode = false
    private val modifiedData = mutableListOf<CloActivityMapping>()
    private var originalData: List<CloActivityMapping> = emptyList()

    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnSubmit: Button
    private lateinit var textViewStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offeredCourseId = it.getInt("offeredCourseId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_c_l_os_to_activities, container, false)

        // Initialize views
        tableHeader = view.findViewById(R.id.tableHeader)
        tableLayout = view.findViewById(R.id.tableLayout)
        tableFooter = view.findViewById(R.id.tableFooter)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnSave = view.findViewById(R.id.btnSave)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        textViewStatus = view.findViewById(R.id.textViewStatus)

        // Hide edit/save buttons initially
        btnEdit.visibility = View.GONE
        btnSave.visibility = View.GONE

        // Set course info
        view.findViewById<TextView>(R.id.textViewCourseName).text =
            "Course: ${arguments?.getString("courseName")}"
        view.findViewById<TextView>(R.id.textViewSection).text =
            "Section: ${arguments?.getString("section")}"
        view.findViewById<TextView>(R.id.textViewSemester).text =
            "Semester: ${arguments?.getInt("semester")}"

        // Button listeners
        btnSubmit.setOnClickListener { toggleEditMode() }
        btnEdit.setOnClickListener { toggleEditMode() }
        btnSave.setOnClickListener { saveChanges() }

        // Load data
        if (offeredCourseId != -1) {
            fetchCloActivityMappings(offeredCourseId)
        }

        return view
    }

    private fun fetchCloActivityMappings(offeredCourseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getCloActivityMappings(offeredCourseId)
                if (response.isSuccessful) {
                    originalData = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        populateTable(originalData)
                        updateStatusView()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Failed to load data")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun populateTable(mappings: List<CloActivityMapping>) {
        tableHeader?.removeAllViews()
        tableLayout?.removeAllViews()
        tableFooter?.removeAllViews()

        if (mappings.isEmpty()) {
            showEmptyDataMessage()
            return
        }

        // Group by Activity Type
        val groupedByActivity = mappings.groupBy { it.activityName }
        val allClos = mappings.map { it.cloId }.distinct().sorted()

        // Create header row
        val headerRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            // Activity header
            addView(TextView(requireContext()).apply {
                text = "Activities"
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            })

            // CLO headers
            allClos.forEach { cloId ->
                addView(TextView(requireContext()).apply {
                    text = "CLO$cloId"
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                    setPadding(16, 16, 16, 16)
                })
            }
        }
        tableHeader?.addView(headerRow)

        // Create data rows (activities as rows)
        groupedByActivity.forEach { (activityType, activityMappings) ->
            val row = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                // Activity label
                addView(TextView(requireContext()).apply {
                    text = activityType
                    gravity = android.view.Gravity.CENTER
                    setPadding(16, 16, 16, 16)
                })

                // CLO percentages
                allClos.forEach { cloId ->
                    val mapping = activityMappings.find { it.cloId == cloId }
                    val percentage = mapping?.percentage ?: 0

                    if (isEditMode) {
                        addView(EditText(requireContext()).apply {
                            setText(percentage.toString())
                            inputType = InputType.TYPE_CLASS_NUMBER
                            gravity = android.view.Gravity.CENTER
                            tag = "$cloId,${mapping?.typeId ?: 0}"
                            setPadding(16, 16, 16, 16)
                        })
                    } else {
                        addView(TextView(requireContext()).apply {
                            text = if (percentage != 0) "$percentage%" else "-"
                            gravity = android.view.Gravity.CENTER
                            setPadding(16, 16, 16, 16)
                        })
                    }
                }
            }
            tableLayout?.addView(row)
        }

        // Add footer row with CLO totals
        val footerRow = TableRow(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            // Footer label
            addView(TextView(requireContext()).apply {
                text = "Total"
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            })

            // Calculate and display totals for each CLO
            allClos.forEach { cloId ->
                val total = mappings.filter { it.cloId == cloId }
                    .sumOf { it.percentage }

                addView(TextView(requireContext()).apply {
                    text = "$total%"
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                    setPadding(16, 16, 16, 16)

                    // Set background based on total value
                    val backgroundRes = when {
                        total == 100 -> R.drawable.green_rounded_box
                        total > 100 -> R.drawable.red_rounded_box
                        else -> R.drawable.yellow_rounded_box
                    }
                    background = ContextCompat.getDrawable(requireContext(), backgroundRes)
                })
            }
        }
        tableFooter?.addView(footerRow)
    }

    private fun updateStatusView() {
        val allClos = originalData.map { it.cloId }.distinct()
        val statusText = StringBuilder()

        allClos.forEach { cloId ->
            val total = originalData.filter { it.cloId == cloId }.sumOf { it.percentage }
            statusText.append("CLO$cloId: $total%\n")
        }

        textViewStatus.text = statusText.toString().trim()
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        if (isEditMode) {
            btnSubmit.visibility = View.GONE
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        } else {
            btnSubmit.visibility = View.VISIBLE
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.GONE
        }
        populateTable(originalData)
    }

    private fun saveChanges() {
        modifiedData.clear()
        var hasErrors = false

        tableLayout?.let { table ->
            // First pass: Validate all inputs
            for (i in 0 until table.childCount) {
                val row = table.getChildAt(i) as? TableRow ?: continue
                val activityName = (row.getChildAt(0) as? TextView)?.text?.toString() ?: continue
                val typeId = originalData.firstOrNull { it.activityName == activityName }?.typeId ?: continue

                for (j in 1 until row.childCount) {
                    val editText = row.getChildAt(j) as? EditText ?: continue
                    val cloId = (tableHeader?.getChildAt(0)?.let { headerRow ->
                        (headerRow as? TableRow)?.getChildAt(j)?.let { headerCell ->
                            (headerCell as? TextView)?.text?.toString()?.removePrefix("CLO")?.toIntOrNull()
                        }
                    } ?: continue)

                    val percentage = editText.text.toString()

                    if (percentage.isNotEmpty()) {
                        val percentageValue = percentage.toIntOrNull() ?: -1

                        when {
                            percentageValue < 0 -> {
                                editText.error = "Invalid value"
                                hasErrors = true
                            }
                            percentageValue > 100 -> {
                                editText.error = "Max 100%"
                                hasErrors = true
                            }
                            else -> {
                                modifiedData.add(
                                    CloActivityMapping(
                                        cloId = cloId,
                                        typeId = typeId,
                                        activityName = activityName,
                                        percentage = percentageValue,
                                        ocId = offeredCourseId
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Second pass: Validate totals
            if (!hasErrors) {
                val allClos = originalData.map { it.cloId }.distinct()
                val validationErrors = mutableMapOf<Int, String>()

                allClos.forEach { cloId ->
                    // Calculate new total for this CLO
                    val originalTotal = originalData
                        .filter { it.cloId == cloId }
                        .sumOf { it.percentage }

                    // Get all modified mappings for this CLO
                    val modifiedForClo = modifiedData.filter { it.cloId == cloId }

                    // Calculate the delta (changes)
                    val delta = modifiedForClo.sumOf { newMapping ->
                        val originalValue = originalData
                            .firstOrNull { it.cloId == newMapping.cloId && it.typeId == newMapping.typeId }
                            ?.percentage ?: 0
                        newMapping.percentage - originalValue
                    }

                    val newTotal = originalTotal + delta

                    if (newTotal > 100) {
                        validationErrors[cloId] = "CLO$cloId total would be $newTotal% (max 100%)"
                    }
                }

                // Show errors if any
                if (validationErrors.isNotEmpty()) {
                    hasErrors = true
                    tableLayout?.let { table ->
                        for (i in 0 until table.childCount) {
                            val row = table.getChildAt(i) as? TableRow ?: continue
                            for (j in 1 until row.childCount) {
                                val editText = row.getChildAt(j) as? EditText ?: continue
                                val cloId = (tableHeader?.getChildAt(0)?.let { headerRow ->
                                    (headerRow as? TableRow)?.getChildAt(j)?.let { headerCell ->
                                        (headerCell as? TextView)?.text?.toString()?.removePrefix("CLO")?.toIntOrNull()
                                    }
                                } ?: continue)

                                validationErrors[cloId]?.let { error ->
                                    editText.error = error
                                }
                            }
                        }
                    }
                }
            }
        }

        if (hasErrors) {
            Toast.makeText(requireContext(), "Please fix validation errors", Toast.LENGTH_LONG).show()
            return
        }

        if (modifiedData.isEmpty()) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            toggleEditMode()
            return
        }

        // Check for any CLO reaching exactly 100%
        val completedClos = modifiedData
            .groupBy { it.cloId }
            .mapValues { (cloId, mappings) ->
                val originalTotal = originalData
                    .filter { it.cloId == cloId }
                    .sumOf { it.percentage }

                val delta = mappings.sumOf { newMapping ->
                    val originalValue = originalData
                        .firstOrNull { it.cloId == newMapping.cloId && it.typeId == newMapping.typeId }
                        ?.percentage ?: 0
                    newMapping.percentage - originalValue
                }

                originalTotal + delta
            }
            .filter { it.value == 100 }
            .keys

        if (completedClos.isNotEmpty()) {
            showCompletionDialog(completedClos)
        } else {
            proceedWithSaving()
        }
    }

    private fun showCompletionDialog(completedClos: Set<Int>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Mapping Complete")
            .setMessage("The following CLOs now have 100% mapping:\n\n" +
                    completedClos.joinToString(", ") { "CLO$it" } +
                    "\n\nDo you want to save these changes?")
            .setPositiveButton("Save") { _, _ ->
                proceedWithSaving()
            }
            .setNegativeButton("Review") { _, _ ->
                // User wants to review, do nothing (stay in edit mode)
            }
            .show()
    }

    private fun proceedWithSaving() {
        btnSave.text = "Saving..."
        btnSave.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val results = mutableListOf<Boolean>()
                modifiedData.forEach { mapping ->
                    val response = RetrofitInstance.apiService.addOrUpdateCloActivityMapping(
                        mapping.ocId,
                        mapping.cloId,
                        mapping.typeId,
                        mapping.percentage
                    )
                    results.add(response.isSuccessful)
                }

                withContext(Dispatchers.Main) {
                    btnSave.text = "Save"
                    btnSave.isEnabled = true

                    if (results.all { it }) {
                        Toast.makeText(requireContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show()
                        fetchCloActivityMappings(offeredCourseId) // Refresh data
                        toggleEditMode()
                    } else {
                        Toast.makeText(requireContext(), "Some changes failed to save", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSave.text = "Save"
                    btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Error saving changes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEmptyDataMessage() {
        tableLayout?.removeAllViews()
        val row = TableRow(requireContext()).apply {
            addView(TextView(requireContext()).apply {
                text = "No CLO-Activity mappings found"
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            })
        }
        tableLayout?.addView(row)
    }

    private fun showError(message: String) {
        tableLayout?.removeAllViews()
        val row = TableRow(requireContext()).apply {
            addView(TextView(requireContext()).apply {
                text = message
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                gravity = android.view.Gravity.CENTER
                setPadding(16, 16, 16, 16)
            })
        }
        tableLayout?.addView(row)
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int
        ): CLOsToActivitiesFragment {
            return CLOsToActivitiesFragment().apply {
                arguments = Bundle().apply {
                    putString("courseCode", courseCode)
                    putString("courseName", courseName)
                    putString("section", section)
                    putInt("semester", semester)
                    putInt("offeredCourseId", offeredCourseId)
                }
            }
        }
    }
}