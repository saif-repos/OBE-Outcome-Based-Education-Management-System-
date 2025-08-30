package com.example.obe_mngt_sys.ACTIVITIES

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.MarkUpdateRequest
import com.example.obe_mngt_sys.MODELS.ProcessedStudentResult
import com.example.obe_mngt_sys.MODELS.QuestionData
import com.example.obe_mngt_sys.MODELS.QuestionInfo
import com.example.obe_mngt_sys.MODELS.TaskResultsResponse
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddResultFragment : Fragment() {
    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var semester: Int? = null
    private var offeredCourseId: Int = -1
    private var taskId: Int = -1
    private var isEditMode = false
    private lateinit var studentResults: List<ProcessedStudentResult>
    private lateinit var questionInfo: List<QuestionInfo>
    private var taskName: String = ""
    private var totalMarks: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString("courseCode")
            courseName = it.getString("courseName")
            section = it.getString("section")
            semester = it.getInt("semester")
            offeredCourseId = it.getInt("offeredCourseId", -1)
            taskId = it.getInt("taskId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set course information
        view.findViewById<TextView>(R.id.textViewCourseInfo).text =
            "$courseName ($courseCode) - Section $section - Semester $semester"

        // Set task ID initially
        view.findViewById<TextView>(R.id.textViewTaskId).text = "Task ID: $taskId"

        // Set up edit button
        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
            toggleEditMode()
        }

        // Load results
        loadTaskResults()
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        val editButton = view?.findViewById<Button>(R.id.btnEdit)

        if (isEditMode) {
            editButton?.text = "SAVE"
            editButton?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            refreshTable()
        } else {
            editButton?.text = "EDIT"
            editButton?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.Aqua))
            saveAllChanges()
        }
    }

    private fun loadTaskResults() {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.textEmptyState)?.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getTaskResults(taskId)
                withContext(Dispatchers.Main) {
                    handleResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    view?.findViewById<TextView>(R.id.textEmptyState)?.visibility = View.VISIBLE
                    Toast.makeText(
                        context,
                        "Error loading results: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("AddResultFragment", "Error loading results", e)
                }
            }
        }
    }


    private fun processRawResponse(raw: TaskResultsResponse): Pair<List<ProcessedStudentResult>, List<QuestionInfo>> {
        val studentResults = raw.results.map { studentMap ->
            ProcessedStudentResult(
                studentId = studentMap["s_id"]?.toString() ?: "",
                studentName = studentMap["student_name"]?.toString() ?: "",
                questionMarks = studentMap
                    .filterKeys { it.startsWith("Q_") }
                    .mapNotNull { (questionHeader, value) ->
                        when (value) {
                            is Map<*, *> -> {
                                questionHeader to QuestionData(
                                    marks = value["marks"]?.toString() ?: "0",
                                    questionId = (value["question_id"] as? Number)?.toInt() ?: 0
                                )
                            }

                            is String -> {
                                questionHeader to QuestionData(
                                    marks = value,
                                    questionId = raw.question_ids?.get(questionHeader) ?: 0
                                )
                            }

                            else -> null
                        }
                    }
                    .toMap()
            )
        }

        val questionInfo = if (!raw.question_ids.isNullOrEmpty()) {
            raw.question_ids.map { (header, id) ->
                QuestionInfo(
                    header = header,
                    maxMarks = header.substringAfter("(").substringBefore(")").toIntOrNull() ?: 0,
                    questionId = id
                )
            }
        } else {
            studentResults.firstOrNull()?.questionMarks?.map { (header, data) ->
                QuestionInfo(
                    header = header,
                    maxMarks = header.substringAfter("(").substringBefore(")").toIntOrNull() ?: 0,
                    questionId = data.questionId
                )
            } ?: emptyList()
        }.sortedBy { it.header }

        return Pair(studentResults, questionInfo)
    }

    private fun handleResponse(response: TaskResultsResponse) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE

        if (response.success && response.results.isNotEmpty()) {
            taskName = response.taskName ?: ""
            totalMarks = response.totalMarks.toInt()  // Use the class property
            val processed = processRawResponse(response)
            studentResults = processed.first
            questionInfo = processed.second
            refreshTable()
        } else {
            view?.findViewById<TextView>(R.id.textEmptyState)?.visibility = View.VISIBLE
        }
    }

    private fun refreshTable() {
        val tableHeader = view?.findViewById<LinearLayout>(R.id.tableHeader)
        val tableRowsContainer = view?.findViewById<LinearLayout>(R.id.tableRowsContainer)

        tableHeader?.removeAllViews()
        tableRowsContainer?.removeAllViews()

        // Calculate weight sum
        tableHeader?.weightSum = 3f + questionInfo.size

        // Add headers
        addHeaderCell(tableHeader, "S_ID", 1)
        addHeaderCell(tableHeader, "Student Name", 2)
        questionInfo.forEach { question ->
            addHeaderCell(tableHeader, question.header, 1)
        }

        // Use the class properties instead of response
        view?.findViewById<TextView>(R.id.textViewTaskId)?.text =
            "Task: $taskName (Total Marks: $totalMarks)"  // Now using the class property

        // Add student rows
        studentResults.sortedBy { it.studentId }.forEach { student ->
            addStudentRow(tableRowsContainer, student, questionInfo)
        }
    }

    private fun addHeaderCell(container: LinearLayout?, text: String, weight: Int) {
        container?.addView(TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                weight.toFloat()
            )
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            this.text = text
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(4, 8, 4, 8)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
    }

    private fun addStudentRow(
        container: LinearLayout?,
        student: ProcessedStudentResult,
        questions: List<QuestionInfo>
    ) {
        val rowLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            weightSum = 3f + questions.size
            background = ContextCompat.getDrawable(requireContext(), R.drawable.row_border)
        }

        // Add student ID with weight 1
        addCell(rowLayout, student.studentId, 1, false)

        // Add student name with weight 2
        addCell(rowLayout, student.studentName, 2, false)

        // Add question marks with weight 1 each
        questions.forEach { question ->
            val marksData = student.questionMarks[question.header]
            val marks = marksData?.marks ?: "0"
            addCell(rowLayout, marks, 1, isEditMode, student.studentId, question.questionId)
        }

        container?.addView(rowLayout)
    }

    private fun addCell(
        rowLayout: LinearLayout,
        text: String,
        weight: Int,
        isEditable: Boolean,
        studentId: String? = null,
        questionId: Int? = null
    ) {
        if (isEditable && studentId != null && questionId != null) {
            val question = questionInfo.find { it.questionId == questionId }
            val maxMarks = question?.maxMarks ?: 0

            val editText = EditText(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight.toFloat()
                )
                setText(text)
                gravity = Gravity.CENTER
                setPadding(4, 8, 4, 8)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                tag = Triple(studentId, questionId, maxMarks) // Store max marks for validation
                setBackgroundResource(R.drawable.edit_text_border)

                // Add text watcher for real-time validation
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val marks = s.toString().toIntOrNull() ?: 0
                        if (marks > maxMarks) {
                            setTextColor(Color.RED)
                            error = "Max: $maxMarks"
                        } else {
                            setTextColor(Color.BLACK)
                            error = null
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            rowLayout.addView(editText)
        } else {
            // Create non-editable TextView
            rowLayout.addView(TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight.toFloat()
                )
                this.text = text
                gravity = Gravity.CENTER
                setPadding(4, 8, 4, 8)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            })
        }
    }

    private fun saveAllChanges() {
        val tableRowsContainer = view?.findViewById<LinearLayout>(R.id.tableRowsContainer)
        val updates = mutableListOf<MarkUpdateRequest>()

        // Collect all changes
        for (i in 0 until (tableRowsContainer?.childCount ?: 0)) {
            val row = tableRowsContainer?.getChildAt(i) as? LinearLayout
            if (row != null) {
                // First view is student ID (TextView)
                val studentId = (row.getChildAt(0) as? TextView)?.text.toString()

                // Skip the first two views (ID and name)
                for (j in 2 until row.childCount) {
                    val cell = row.getChildAt(j)
                    if (cell is EditText) {
                        val (_, questionId, maxMarks) = cell.tag as? Triple<*, *, *> ?: continue
                        val marksText = cell.text.toString()
                        val marksValue = marksText.toIntOrNull() ?: 0 // Convert to Int

                        updates.add(
                            MarkUpdateRequest(
                                StudentId = studentId,
                                QuestionId = questionId as Int,
                                Marks = marksValue,  // Now sending as Int
                                TaskId = taskId
                            )
                        )
                    }
                }
            }
        }

        if (updates.isNotEmpty()) {
            saveChangesToServer(updates)
        } else {
            refreshTable()
        }
    }
    private fun saveChangesToServer(updates: List<MarkUpdateRequest>) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                updates.forEach { update ->
                    Log.d("API_DEBUG", "Sending: $update")
                    val response = RetrofitInstance.apiService.updateQuestionMarks(update)

                    if (!response.isSuccessful) {
                        Log.e("API_ERROR", "Failed for Q${update.QuestionId}: ${response.code()}")
                    }
                }

                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(context, "Marks updated", Toast.LENGTH_SHORT).show()
                    loadTaskResults() // Refresh data
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                    Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
                    Log.e("NETWORK", "Error: ${e.message}")
                }
            }
        }
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            taskId: Int
        ): AddResultFragment {
            return AddResultFragment().apply {
                arguments = Bundle().apply {
                    putString("courseCode", courseCode)
                    putString("courseName", courseName)
                    putString("section", section)
                    putInt("semester", semester)
                    putInt("offeredCourseId", offeredCourseId)
                    putInt("taskId", taskId)

                }
            }
        }
    }
}