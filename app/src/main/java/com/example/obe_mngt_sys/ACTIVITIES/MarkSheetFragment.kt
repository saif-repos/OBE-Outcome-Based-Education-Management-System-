//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.graphics.Typeface
//import android.os.Bundle
//import android.util.Log
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.R
//import com.example.obe_mngt_sys.MODELS.CourseResults
//import com.example.obe_mngt_sys.MODELS.CourseResultsResponse
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class MarkSheetFragment : Fragment() {
//
//    private var courseCode: String? = null
//    private var courseName: String? = null
//    private var section: String? = null
//    private var semester: Int? = null
//    private var offeredCourseId: Int = -1
//
//    private lateinit var headerContainer: LinearLayout
//    private lateinit var studentRowsContainer: LinearLayout
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            courseCode = it.getString("courseCode")
//            courseName = it.getString("courseName")
//            section = it.getString("section")
//            semester = it.getInt("semester")
//            offeredCourseId = it.getInt("offeredCourseId", -1)
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_mark_sheet, container, false)
//
//        headerContainer = view.findViewById(R.id.header_container)
//        studentRowsContainer = view.findViewById(R.id.student_rows_container)
//
//        fetchCourseResults()
//
//        return view
//    }
//
//    private fun fetchCourseResults() {
//        val call = RetrofitInstance.apiService.GetCourseResults(offeredCourseId)
//        call.enqueue(object : Callback<CourseResultsResponse> {
//            override fun onResponse(
//                call: Call<CourseResultsResponse>,
//                response: Response<CourseResultsResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    if (body?.success == true && body.courseResults != null) {
//                        displayCourseResults(body.courseResults)
//                    } else {
//                        showError("No course results data available")
//                    }
//                } else {
//                    showError("Failed to load course results")
//                }
//            }
//
//            override fun onFailure(call: Call<CourseResultsResponse>, t: Throwable) {
//                showError("Network error: ${t.message}")
//            }
//        })
//    }
//
//    private fun displayCourseResults(results: CourseResults) {
//        try {
//            headerContainer.removeAllViews()
//            studentRowsContainer.removeAllViews()
//
//            // First row: Task names as spans
//            val taskRow = LinearLayout(requireContext()).apply {
//                orientation = LinearLayout.HORIZONTAL
//            }
//            taskRow.addView(createHeaderCell("Student", 150, true))
//
//            results.tasks?.forEach { task ->
//                val questionCount = task.questions?.size ?: 0
//                val width = 150 * questionCount
//                taskRow.addView(createHeaderCell(task.taskName ?: "Task", width, true))
//            }
//            headerContainer.addView(taskRow)
//
//            // Second row: Question headers
//            val questionRow = LinearLayout(requireContext()).apply {
//                orientation = LinearLayout.HORIZONTAL
//            }
//            questionRow.addView(createHeaderCell("", 150, false)) // Empty cell under "Student"
//            results.tasks?.forEach { task ->
//                task.questions?.forEach { question ->
//                    questionRow.addView(createHeaderCell(question.que_No ?: "Q", 150, false))
//                }
//            }
//            headerContainer.addView(questionRow)
//
//            // Student data rows
//            results.studentResults?.forEach { student ->
//                val studentRow = LinearLayout(requireContext()).apply {
//                    orientation = LinearLayout.HORIZONTAL
//                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.row_background))
//                }
//
//                studentRow.addView(createStudentCell(student.student_name ?: "Unknown", true))
//
//                results.tasks?.forEach { task ->
//                    task.questions?.forEach { question ->
//                        val key = "${task.taskName?.trim()}_${question.que_No?.trim()}"
//                        val result = student.results?.get(key)
//                        if (result == null) {
//                            Log.w("MARK_FETCH", "No result found for key: $key")
//                        }
//                        val marks = result?.marks ?: "0"
//
//                        studentRow.addView(createStudentCell(marks, false))
//
//
//                    }
//                }
//
//                studentRowsContainer.addView(studentRow)
//            }
//
//        } catch (e: Exception) {
//            Log.e("MarkSheetFragment", "Error displaying results", e)
//            showError("Error displaying results: ${e.message}")
//        }
//    }
//
//    private fun createHeaderCell(text: String, width: Int, isMainHeader: Boolean): View {
//        val context = requireContext()
//        return TextView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)
//            this.text = text
//            textSize = if (isMainHeader) 14f else 12f
//            setTypeface(null, if (isMainHeader) Typeface.BOLD else Typeface.NORMAL)
//            gravity = Gravity.CENTER
//            setPadding(8, 12, 8, 12)
//            setTextColor(ContextCompat.getColor(context, R.color.white))
//            setBackgroundColor(
//                ContextCompat.getColor(
//                    context,
//                    if (isMainHeader) R.color.header_background else R.color.subheader_background
//                )
//            )
//        }
//    }
//
//    private fun createStudentCell(text: String, isNameCell: Boolean): View {
//        val context = requireContext()
//        return TextView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT)
//            this.text = text
//            textSize = 12f
//            gravity = Gravity.CENTER
//            setPadding(8, 12, 8, 12)
//            setTextColor(
//                ContextCompat.getColor(
//                    context,
//                    if (isNameCell) R.color.primary_text else R.color.secondary_text
//                )
//            )
//            setBackgroundColor(
//                ContextCompat.getColor(
//                    context,
//                    if (isNameCell) R.color.name_cell_background else R.color.mark_cell_background
//                )
//            )
//        }
//    }
//
//    private fun showError(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
//        Log.e("MarkSheetFragment", message)
//    }
//
//    companion object {
//        fun newInstance(
//            courseCode: String,
//            courseName: String,
//            section: String,
//            semester: Int,
//            offeredCourseId: Int
//        ): MarkSheetFragment {
//            return MarkSheetFragment().apply {
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

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.MODELS.CourseResults
import com.example.obe_mngt_sys.MODELS.CourseResultsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Make sure TaskInfo is imported if it's in a different file but same package
// import com.example.obe_mngt_sys.MODELS.TaskInfo

class MarkSheetFragment : Fragment() {

    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var semester: Int? = null // semester is Int in arguments, but not used in API call
    private var offeredCourseId: Int = -1

    private lateinit var headerContainer: LinearLayout
    private lateinit var studentRowsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString("courseCode")
            courseName = it.getString("courseName")
            section = it.getString("section")
            semester = it.getInt("semester") // Received but not directly used in fetch
            offeredCourseId = it.getInt("offeredCourseId", -1)
        }
        Log.d("MarkSheetFragment", "onCreate: offeredCourseId = $offeredCourseId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mark_sheet, container, false)

        headerContainer = view.findViewById(R.id.header_container)
        studentRowsContainer = view.findViewById(R.id.student_rows_container)

        if (offeredCourseId == -1) {
            showError("Error: Course ID not provided to MarkSheetFragment.")
            Log.e("MarkSheetFragment", "offeredCourseId is -1 in onCreateView")
        } else {
            fetchCourseResults()
        }

        return view
    }

    private fun fetchCourseResults() {
        if (offeredCourseId == -1) {
            Log.e("MarkSheetFragment", "Cannot fetch results, offeredCourseId is -1.")
            showError("Invalid Course ID for fetching results.")
            return
        }
        Log.d("MarkSheetFragment", "Fetching course results for offeredCourseId: $offeredCourseId")

        val call = RetrofitInstance.apiService.GetCourseResults(offeredCourseId)
        call.enqueue(object : Callback<CourseResultsResponse> {
            override fun onResponse(
                call: Call<CourseResultsResponse>,
                response: Response<CourseResultsResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.courseResults != null) {
                        Log.d("MarkSheetFragment", "Successfully fetched course results.")
                        // Log the raw response for debugging if needed
                        // Log.d("MarkSheetFragment_RAW_RESPONSE", response.raw().toString())
                        // Log.d("MarkSheetFragment_BODY", com.google.gson.Gson().toJson(body))
                        displayCourseResults(body.courseResults)
                    } else {
                        val errorMsg = body?.message ?: body?.error ?: "No course results data available or request not successful."
                        showError(errorMsg)
                        Log.e("MarkSheetFragment", "API success false or null courseResults: $errorMsg")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Failed to load course results. Code: ${response.code()}. Message: ${response.message()}. Error: $errorBody")
                    Log.e("MarkSheetFragment", "API call failed: ${response.code()} - $errorBody")
                }
            }

            override fun onFailure(call: Call<CourseResultsResponse>, t: Throwable) {
                showError("Network error: ${t.message}")
                Log.e("MarkSheetFragment", "API call onFailure: ${t.message}", t)
            }
        })
    }

    private fun displayCourseResults(results: CourseResults) {
        try {
            headerContainer.removeAllViews()
            studentRowsContainer.removeAllViews()

            // --- DEBUGGING: Log what's received ---
            // results.tasks?.forEach { task ->
            //     Log.d("MarkSheet_TaskInfo", "Task: ${task.taskName}, Questions: ${task.questions?.size}")
            //     task.questions?.forEach { q ->
            //         Log.d("MarkSheet_TaskQuestion", "  Q: ${q.que_No} (ID: ${q.tq_id})")
            //     }
            // }
            // results.studentResults?.firstOrNull()?.let { student ->
            //      Log.d("MarkSheet_StudentSample", "First student: ${student.s_id}, Name: ${student.student_name}")
            //      Log.d("MarkSheet_StudentSampleKeys", "Keys in first student's results map: ${student.results?.keys}")
            // }
            // --- END DEBUGGING ---


            // First row: Task names as spans
            val taskRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            taskRow.addView(createHeaderCell("Student", 150, true, 0)) // Added spanCount for consistency

            results.tasks?.forEach { task ->
                val questionCount = task.questions?.size ?: 0
                if (questionCount > 0) { // Only add task header if it has questions
                    val taskWidth = 150 * questionCount // Fixed width per question
                    taskRow.addView(createHeaderCell(task.taskName ?: "Task", taskWidth, true, questionCount))
                }
            }
            headerContainer.addView(taskRow)

            // Second row: Question headers
            val questionRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            // Empty cell under "Student"
            questionRow.addView(createHeaderCell("", 150, false, 1))

            results.tasks?.forEach { task ->
                task.questions?.forEach { question -> // This will naturally skip tasks with no questions
                    questionRow.addView(createHeaderCell(question.que_No?.trim() ?: "Q", 150, false, 1))
                }
            }
            headerContainer.addView(questionRow)

            // Student data rows
            results.studentResults?.forEach { student ->
                val studentRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    // Consider adding alternating row colors or borders for better readability
                    // setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.row_background))
                }

                studentRow.addView(createStudentCell(student.student_name?.trim() ?: "Unknown", true))

                // Iterate through the tasks in the same order as headers
                results.tasks?.forEach { task ->
                    task.questions?.forEach { question ->
                        // Key construction must exactly match the keys in student.results Map from the API
                        val taskNameKey = task.taskName?.trim() // Already trimmed in API, but good practice
                        val queNoKey = question.que_No?.trim()   // Already trimmed in API

                        val key = "${taskNameKey}_${queNoKey}"

                        val questionResult = student.results?.get(key)

                        if (questionResult == null) {
                            // This log should now appear less or only for genuinely missing data
                            Log.w("MARK_FETCH", "No result found for key: '$key' for student: ${student.s_id}. Available keys: ${student.results?.keys}")
                        }
                        val marks = questionResult?.marks ?: "0" // Default to "0" if not found

                        studentRow.addView(createStudentCell(marks, false))
                    }
                }
                studentRowsContainer.addView(studentRow)
            }

        } catch (e: Exception) {
            Log.e("MarkSheetFragment", "Error displaying results", e)
            showError("Error displaying results: ${e.message}")
        }
    }

    // Updated createHeaderCell to optionally take spanCount if you were to use GridLayout later
    // For LinearLayout, spanCount isn't directly used for width calculation here,
    // but kept for consistency if you had other plans.
    // Width is now more directly controlled.
    private fun createHeaderCell(text: String, widthInDp: Int, isMainHeader: Boolean, spanCount: Int): View {
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val widthInPixels = (widthInDp * density).toInt()

        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(widthInPixels, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.text = text
            textSize = if (isMainHeader) 14f else 12f
            setTypeface(null, if (isMainHeader) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER
            setPadding((8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt())
            setTextColor(ContextCompat.getColor(context, R.color.white)) // Make sure R.color.white is defined
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isMainHeader) R.color.header_background else R.color.subheader_background // Define these colors
                )
            )
            // Example of adding a border
            // val border = GradientDrawable()
            // border.setColor(ContextCompat.getColor(context, if (isMainHeader) R.color.header_background else R.color.subheader_background))
            // border.setStroke(1, Color.GRAY)
            // background = border
        }
    }

    private fun createStudentCell(text: String, isNameCell: Boolean): View {
        val context = requireContext()
        val density = context.resources.displayMetrics.density
        val widthInPixels = (150 * density).toInt() // Fixed width for student cells (name and mark)

        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(widthInPixels, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.text = text
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding((8 * density).toInt(), (12 * density).toInt(), (8 * density).toInt(), (12 * density).toInt())
            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (isNameCell) R.color.primary_text else R.color.secondary_text // Define these colors
                )
            )
            // Example: Set background based on cell type, or add borders
            // val border = GradientDrawable()
            // border.setColor(ContextCompat.getColor(context, if (isNameCell) R.color.name_cell_background else R.color.mark_cell_background)) // Define these
            // border.setStroke(1, Color.LTGRAY)
            // background = border
            // For simplicity, keeping original background logic if colors are defined
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (isNameCell) R.color.name_cell_background else R.color.mark_cell_background
                )
            )
        }
    }

    private fun showError(message: String) {
        if (isAdded && context != null) { // Check if fragment is added to an activity and context is available
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
        Log.e("MarkSheetFragment", message)
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int
        ): MarkSheetFragment {
            return MarkSheetFragment().apply {
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