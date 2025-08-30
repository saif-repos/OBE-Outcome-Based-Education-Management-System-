package com.example.obe_mngt_sys.ACTIVITIES

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.CoursePLOResponse
import com.example.obe_mngt_sys.MODELS.EditableCoursePLO
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class fragment_Mapping : Fragment() {

    private lateinit var gridLayout: GridLayout
    private lateinit var editButton: Button
    private lateinit var saveButton: Button
    private var programId: Int = -1  // Store program ID
    private var maxPLOCount: Int = 0 // Dynamically determine max PLO count
    private var isEditable = false   // Track if grid is editable
    private lateinit var coursePLOList: List<CoursePLOResponse> // Store course-PLO data

    // Custom data structure to track grid cells
    private data class GridCell(
        val row: Int,
        val ploId: Int, // Changed column to ploId for clarity
        val editText: EditText
    )

    private val gridCells = mutableListOf<GridCell>() // List to store grid cells

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            programId = it.getInt(ARG_PROGRAM_ID, -1)  // Retrieve programId
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment__mapping, container, false)

        // Initialize GridLayout and Buttons
        gridLayout = view.findViewById(R.id.gridLayout)
        editButton = view.findViewById(R.id.editButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Fetch data from API
        if (programId != -1) {
            fetchCoursePLOMapping(programId)
        }

        // Set Edit Button Click Listener
        editButton.setOnClickListener {
            isEditable = true
            populateGridLayout(coursePLOList, isEditable)
            saveButton.visibility = View.VISIBLE // Show Save button
            editButton.visibility = View.GONE    // Hide Edit button
        }

        // Set Save Button Click Listener
        saveButton.setOnClickListener {
            // No longer directly setting isEditable = false here.
            // It will be set after successful save or if no changes.
            saveChanges()
        }

        return view
    }

    private fun fetchCoursePLOMapping(programId: Int) {
        val apiService = RetrofitInstance.apiService

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<List<CoursePLOResponse>> =
                    apiService.GetCoursesWithMappedPLOs(programId)
                if (response.isSuccessful) {
                    coursePLOList = response.body() ?: emptyList()
                    if (coursePLOList.isNotEmpty()) {
                        // Find max PLO count dynamically
                        maxPLOCount =
                            coursePLOList.flatMap { it.PLOs.map { plo -> plo.PLOId } }.maxOrNull()
                                ?: 0

                        withContext(Dispatchers.Main) {
                            populateGridLayout(coursePLOList, isEditable)
                        }
                    } else {
                        Log.e("MappingDebug", "Empty CoursePLOList received")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "No course mapping data found.",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Also clear the grid if no data
                            (view?.findViewById<GridLayout>(R.id.gridHeader))?.removeAllViews()
                            gridLayout.removeAllViews()
                            editButton.visibility = View.GONE // Hide edit button if no data
                            saveButton.visibility = View.GONE // Hide save button
                        }
                    }
                } else {
                    Log.e("MappingDebug", "API Response Failed: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch data: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Clear grid on error
                        (view?.findViewById<GridLayout>(R.id.gridHeader))?.removeAllViews()
                        gridLayout.removeAllViews()
                        editButton.visibility = View.GONE // Hide edit button on error
                        saveButton.visibility = View.GONE // Hide save button
                    }
                }
            } catch (e: Exception) {
                Log.e("MappingDebug", "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Clear grid on network error
                    (view?.findViewById<GridLayout>(R.id.gridHeader))?.removeAllViews()
                    gridLayout.removeAllViews()
                    editButton.visibility = View.GONE // Hide edit button on network error
                    saveButton.visibility = View.GONE // Hide save button
                }
            }
        }
    }

    private fun populateGridLayout(coursePLOList: List<CoursePLOResponse>, isEditable: Boolean) {
        val gridHeader = view?.findViewById<GridLayout>(R.id.gridHeader)
        gridHeader?.removeAllViews()
        gridLayout.removeAllViews()
        gridCells.clear()

        val allPLOIds = coursePLOList.flatMap { it.PLOs.map { plo -> plo.PLOId } }.distinct().sorted()
        val columnCount = allPLOIds.size + 1 // +1 for the Course column
        gridHeader?.columnCount = columnCount
        gridLayout.columnCount = columnCount

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val columnWidth = (screenWidth - dpToPx(20)) / columnCount // Use dpToPx for margin

        val ploColumnMap = mutableMapOf<Int, Int>().apply {
            put(-1, 0) // Placeholder for course column index
            allPLOIds.forEachIndexed { index, ploId -> put(ploId, index + 1) } // PLOs start from index 1
        }

        // --- Header Row ---
        // Course Header
        val courseHeader = TextView(requireContext()).apply {
            text = "Course"
            gravity = Gravity.CENTER
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            textSize = 8f
            setTextColor(resources.getColor(android.R.color.black, null))
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.LTGRAY)
        }
        gridHeader?.addView(courseHeader, GridLayout.LayoutParams().apply {
            width = columnWidth
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(0)
            rowSpec = GridLayout.spec(0)
        })

        // PLO Headers
        allPLOIds.forEach { ploId ->
            val ploHeader = TextView(requireContext()).apply {
                text = "$ploId"
                gravity = Gravity.CENTER
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                textSize = 8f
                setTextColor(resources.getColor(android.R.color.black, null))
                setTypeface(null, Typeface.BOLD)
                setBackgroundColor(Color.LTGRAY)
            }
            gridHeader?.addView(ploHeader, GridLayout.LayoutParams().apply {
                width = columnWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(ploColumnMap[ploId] ?: 0)
                rowSpec = GridLayout.spec(0)
            })
        }

        // --- Populate Grid Rows ---
        for ((index, course) in coursePLOList.withIndex()) {
            val rowIndex = index + 1 // +1 for the header row

            // Course Name TextView
            val courseNameTextView = TextView(requireContext()).apply {
                text = course.CourseName ?: "N/A"
                gravity = Gravity.CENTER
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                textSize = 8f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setBackgroundColor(Color.LTGRAY)
                // Add OnClickListener to show full name in a Toast
                setOnClickListener {
                    Toast.makeText(requireContext(), course.CourseName, Toast.LENGTH_LONG).show()
                }
            }
            gridLayout.addView(courseNameTextView, GridLayout.LayoutParams().apply {
                width = columnWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(0)
                rowSpec = GridLayout.spec(rowIndex)
            })

            for (plo in course.PLOs) {
                val columnIndex = ploColumnMap[plo.PLOId] ?: continue
                val ploView = if (isEditable) {
                    EditText(requireContext()).apply {
                        setText("${plo.Percentage}")
                        gravity = Gravity.CENTER
                        setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                        textSize = 8f
                        inputType = InputType.TYPE_CLASS_NUMBER
                        setBackgroundResource(R.drawable.edit_text_border) // Re-use existing border
                    }
                } else {
                    TextView(requireContext()).apply {
                        text = "${plo.Percentage}"
                        gravity = Gravity.CENTER
                        setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                        textSize = 8f
                    }
                }

                gridLayout.addView(ploView, GridLayout.LayoutParams().apply {
                    width = columnWidth
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(columnIndex)
                    rowSpec = GridLayout.spec(rowIndex)
                })

                if (ploView is EditText) {
                    gridCells.add(GridCell(rowIndex, plo.PLOId, ploView))
                }
            }
        }


        // --- Calculate PLO Sums ---
        val ploSumMap = mutableMapOf<Int, Int>()
        allPLOIds.forEach { ploSumMap[it] = 0 }
        for (course in coursePLOList) {
            for (plo in course.PLOs) {
                ploSumMap[plo.PLOId] = ploSumMap[plo.PLOId]!! + plo.Percentage.toInt()
            }
        }

        val sumRowIndex = coursePLOList.size + 1 // The row after the last course

        // Sum Row Label
        val sumLabel = TextView(requireContext()).apply {
            text = "PER"
            gravity = Gravity.CENTER
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            textSize = 8f
            setTextColor(resources.getColor(android.R.color.black, null))
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(Color.LTGRAY)
        }
        gridLayout.addView(sumLabel, GridLayout.LayoutParams().apply {
            width = columnWidth
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(0)
            rowSpec = GridLayout.spec(sumRowIndex)
        })

        // Sum Values
        allPLOIds.forEach { ploId ->
            val columnIndex = ploColumnMap[ploId] ?: return@forEach
            val sumValue = ploSumMap[ploId] ?: 0

            val sumTextView = TextView(requireContext()).apply {
                text = "$sumValue"
                gravity = Gravity.CENTER
                setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
                textSize = 8f
                setTextColor(Color.WHITE)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                setTypeface(null, Typeface.BOLD)
                background = if (sumValue >= 50) {
                    resources.getDrawable(R.drawable.green_rounded_box, null)
                } else {
                    resources.getDrawable(R.drawable.red_rounded_box, null)
                }
            }
            gridLayout.addView(sumTextView, GridLayout.LayoutParams().apply {
                width = columnWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(columnIndex)
                rowSpec = GridLayout.spec(sumRowIndex)
            })
        }
    }

    private fun saveChanges() {
        val updatedData = mutableListOf<EditableCoursePLO>()
        val ploColumnCurrentSums = mutableMapOf<Int, Int>() // To track current sums for each PLO column

        // Initialize ploColumnCurrentSums based on original data (before edits)
        val allPLOIds = coursePLOList.flatMap { it.PLOs.map { plo -> plo.PLOId } }.distinct().sorted()
        allPLOIds.forEach { ploId ->
            ploColumnCurrentSums[ploId] = coursePLOList.sumOf { course ->
                course.PLOs.firstOrNull { it.PLOId == ploId }?.Percentage?.toInt() ?: 0
            }
        }


        var hasIndividualCellErrors = false
        var hasPloSumExceedance = false

        for (cell in gridCells) {
            val row = cell.row
            val ploId = cell.ploId

            val courseIndex = row - 1
            if (courseIndex !in coursePLOList.indices) {
                Log.e("SaveChanges", "Invalid course index: $courseIndex for PLOId: $ploId")
                continue
            }
            val course = coursePLOList[courseIndex]

            val newPercentageStr = cell.editText.text.toString()
            val newPercentage = newPercentageStr.toIntOrNull()

            // --- Individual cell validation (0-100 and integer format) ---
            if (newPercentage == null || newPercentage < 0 || newPercentage > 100) {
                cell.editText.error = "0-100" // Set error directly on the EditText
                hasIndividualCellErrors = true
                continue // Skip to the next cell if invalid
            } else {
                cell.editText.error = null // Clear error if valid
            }

            // Get the old percentage for this specific mapping
            val oldPercentageForMapping = course.PLOs.firstOrNull { it.PLOId == ploId }?.Percentage?.toInt() ?: 0

            // Calculate the adjusted total for this PLO column
            // Remove old percentage from total and add new one
            val adjustedTotal = (ploColumnCurrentSums[ploId] ?: 0) - oldPercentageForMapping + newPercentage

            // --- PLO Column 100% Exceedance Validation ---
            if (adjustedTotal > 100) {
                // Set error on the current cell, but the Toast will be more prominent
                cell.editText.error = "Sum > 100!"
                Toast.makeText(
                    requireContext(),
                    "PLO $ploId total cannot exceed 100%. Current total: $adjustedTotal%",
                    Toast.LENGTH_LONG
                ).show()
                hasPloSumExceedance = true
                // We'll continue iterating to highlight all errors, but stop the save process later
            } else {
                // Only update the running total if it doesn't exceed 100% *from this specific update*
                ploColumnCurrentSums[ploId] = adjustedTotal
            }

            // Add to updatedData only if valid so far
            if (!hasIndividualCellErrors && !hasPloSumExceedance) {
                updatedData.add(EditableCoursePLO(course.CourseCode, ploId, newPercentage))
            }
        }

        // If any errors were found (either individual cell or PLO sum exceedance), stop here.
        if (hasIndividualCellErrors || hasPloSumExceedance) {
            Toast.makeText(
                requireContext(),
                "Please fix the highlighted errors before saving.",
                Toast.LENGTH_LONG
            ).show()
            // Keep in edit mode so user can see errors
            return
        }

        if (updatedData.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "No changes detected to save.",
                Toast.LENGTH_SHORT
            ).show()
            isEditable = false
            saveButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE
            populateGridLayout(coursePLOList, isEditable) // Re-populate to view mode
            return
        }

        updateDataOnServer(updatedData)
    }


    private fun updateDataOnServer(updatedData: List<EditableCoursePLO>) {
        // Temporarily disable buttons to prevent multiple clicks
        saveButton.isEnabled = false
        editButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var allSuccessful = true
                for (data in updatedData) {
                    Log.d(
                        "SaveChanges",
                        "Updating ${data.courseCode} - PLO ${data.ploId} to ${data.percentage}%"
                    )

                    val response = RetrofitInstance.apiService.updateCourseMapPlo(
                        P_ID = programId,
                        C_code = data.courseCode,
                        PLO_ID = data.ploId,
                        per = data.percentage
                    )

                    if (response.isSuccessful) {
                        Log.d(
                            "SaveChanges",
                            "Successfully updated ${data.courseCode} - PLO ${data.ploId} to ${data.percentage}%"
                        )
                    } else {
                        allSuccessful = false
                        val errorBody = response.errorBody()?.string()
                        Log.e(
                            "SaveChanges",
                            "Failed to update ${data.courseCode} - PLO ${data.ploId}. Error: ${response.code()} - $errorBody"
                        )
                        // If one fails, no need to continue for others to avoid inconsistent state on backend
                        break
                    }
                }

                withContext(Dispatchers.Main) {
                    if (allSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Changes saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        isEditable = false // Exit edit mode only on full success
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Some changes failed to save. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Remain in edit mode if not all successful to allow user to retry
                        isEditable = true
                    }
                    saveButton.visibility = View.GONE
                    editButton.visibility = View.VISIBLE
                    // Re-enable buttons
                    saveButton.isEnabled = true
                    editButton.isEnabled = true
                    fetchCoursePLOMapping(programId) // Refresh data from the server
                }
            } catch (e: Exception) {
                Log.e("SaveChanges", "Error saving changes", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error saving changes: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    // Re-enable buttons and stay in edit mode on network error
                    saveButton.isEnabled = true
                    editButton.isEnabled = true
                    isEditable = true
                }
            }
        }
    }

    // Helper to convert DP to Pixels
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val ARG_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Int): fragment_Mapping {
            return fragment_Mapping().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PROGRAM_ID, programId)
                }
            }
        }
    }
}