//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.*
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.obe_mngt_sys.R
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.MODELS.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import retrofit2.Response
//import android.app.Dialog
//import android.graphics.Typeface
//import android.text.InputType // Import InputType for EditText
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//
//
//class TASK_QUESTIONS : AppCompatActivity() {
//
//    private var questionCount = 1
//    private lateinit var questionsContainer: LinearLayout
//    private lateinit var totalMarksEditText: EditText
//    private lateinit var saveButton: Button
//    private lateinit var updateButton: Button
//    private var taskId: Int = -1
//    private var taskIdentifier: String? = null
//    private var smallTaskId: Int? = null
//    private var isEditMode = false
//    private var existingQuestions = mutableListOf<QuestionDetails>()
//    private var offeredCourseId: Int = -1
//    private var mainActivityId: Int = -1
//    private var subActivityId: Int = -1
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_task_questions)
//
//        // Initialize views
//        questionsContainer = findViewById(R.id.questionsContainer)
//        val addQuestionButton = findViewById<Button>(R.id.addQuestionButton)
//        saveButton = findViewById(R.id.saveButton)
//        updateButton = findViewById(R.id.updateButton)
//        totalMarksEditText = findViewById(R.id.totalMarksEditText)
//
//        // Get the passed data
//        mainActivityId = intent.getIntExtra("MAIN_ACTIVITY_ID", -1)
//        val mainActivityName = intent.getStringExtra("MAIN_ACTIVITY_NAME") ?: ""
//        subActivityId = intent.getIntExtra("SUB_ACTIVITY_ID", -1)
//        val subActivityName = intent.getStringExtra("SUB_ACTIVITY_NAME") ?: ""
//        offeredCourseId = intent.getIntExtra("OFFERED_COURSE_ID", -1)
//        taskId = intent.getIntExtra("TSK_ID", -1).takeIf { it != -1 }
//            ?: intent.getIntExtra("TASK_ID", -1)
//
//        Log.d("TASK_DEBUG", "Received task ID: $taskId")
//
//        // Check if we're in edit mode
//        isEditMode = taskId != -1
//
//        // Update the title
//        val textViewTeacherName = findViewById<TextView>(R.id.textViewTeacherName)
//        textViewTeacherName.text = if (subActivityId != -1) {
//            "$mainActivityName - $subActivityName"
//        } else {
//            mainActivityName
//        }
//
//        // Set up back button
//        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
//            finish()
//        }
//
//        // Setup appropriate mode
//        if (isEditMode) {
//            setupEditMode()
//        } else {
//            setupCreateMode()
//        }
//
//        // Add question button click listener
//        addQuestionButton.setOnClickListener {
//            addQuestionField()
//        }
//
//        // Save button click listener
//        saveButton.setOnClickListener {
//            saveQuestions()
//        }
//
//        // Update button click listener
//        updateButton.setOnClickListener {
//            updateQuestions()
//        }
//
//        // Add a button for CLO Mapping (assuming you'll add one in your layout)
//        // For demonstration, let's assume you have a button with ID 'btnCloMapping'
//        // If not, you'll need to add it to your activity_task_questions.xml
//        findViewById<Button>(R.id.mappingButton)?.setOnClickListener {
//            showCloMappingDialog()
//        }
//
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//
//    private fun setupEditMode() {
//        saveButton.visibility = View.GONE
//        updateButton.visibility = View.VISIBLE
//        loadExistingTaskDetails()
//    }
//
//    private fun setupCreateMode() {
//        saveButton.visibility = View.VISIBLE
//        updateButton.visibility = View.GONE
//        addQuestionField() // Add first empty question
//    }
//
//    private fun loadExistingTaskDetails() {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.apiService.getTaskDetails(taskId)
//                if (response.isSuccessful) {
//                    response.body()?.let { taskDetails ->
//                        withContext(Dispatchers.Main) {
//                            // Set task total marks
//                            totalMarksEditText.setText(taskDetails.Task.tMarks.toString())
//
//                            // Store existing questions
//                            existingQuestions.clear()
//                            existingQuestions.addAll(taskDetails.Questions)
//
//                            // Add question fields for each existing question
//                            taskDetails.Questions.forEach { question ->
//                                addQuestionField(question.tq_id, question.que, question.tMarks)
//                            }
//                        }
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            this@TASK_QUESTIONS,
//                            "Failed to load task details",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@TASK_QUESTIONS,
//                        "Error: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.e("TASK_QUESTIONS", "Error loading task details", e)
//                }
//            }
//        }
//    }
//
//    private fun addQuestionField(
//        questionId: Int = 0,
//        questionText: String = "",
//        marks: Float = 0f
//    ) {
//        val questionView = LayoutInflater.from(this).inflate(R.layout.question_item, null)
//
//        // Set question number
//        val questionNumber = questionCount++
//        val questionLabel = questionView.findViewById<TextView>(R.id.questionLabel)
//        questionLabel.text = "Question $questionNumber"
//
//        // Set question data if in edit mode
//        if (isEditMode && questionId != 0) {
//            questionView.findViewById<EditText>(R.id.questionEditText).setText(questionText)
//            questionView.findViewById<EditText>(R.id.questionMarksEditText)
//                .setText(marks.toString())
//            questionView.tag = questionId // Store question ID in view tag
//        }
//
//        // Get question and marks fields
//        val questionEditText = questionView.findViewById<EditText>(R.id.questionEditText)
//        val questionMarksEditText = questionView.findViewById<EditText>(R.id.questionMarksEditText)
//
//        // Add text watcher to validate marks
//        questionMarksEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                validateQuestionMarks(questionMarksEditText)
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//
//        // Add delete button functionality
//        val deleteButton = questionView.findViewById<ImageButton>(R.id.deleteButton)
//        deleteButton.setOnClickListener {
//            questionsContainer.removeView(questionView)
//            renumberQuestions()
//        }
//
//        questionsContainer.addView(questionView)
//    }
//
//
//    private fun validateQuestionMarks(marksEditText: EditText) {
//        try {
//            val marks = marksEditText.text.toString().toInt()
//            if (marks < 0) {
//                marksEditText.error = "Marks cannot be negative"
//            }
//        } catch (e: NumberFormatException) {
//            // Handle empty or invalid input
//        }
//    }
//
//    private fun renumberQuestions() {
//        questionCount = 1
//        for (i in 0 until questionsContainer.childCount) {
//            val view = questionsContainer.getChildAt(i)
//            val questionLabel = view.findViewById<TextView>(R.id.questionLabel)
//            questionLabel.text = "Question ${questionCount++}"
//        }
//        questionCount = questionsContainer.childCount + 1
//    }
//
//    private fun saveQuestions() {
//
//        CoroutineScope(Dispatchers.Main).launch {
//            val totalMarks = try {
//                totalMarksEditText.text.toString().toFloat()
//            } catch (e: NumberFormatException) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Please enter valid total marks",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            if (questionsContainer.childCount == 0) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Please add at least one question",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            // Step 1: Add the task
//            val taskCreated = withContext(Dispatchers.IO) { addTaskToBackend() }
//            if (!taskCreated) {
//                return@launch
//            }
//
//            // Step 2: Add questions if task was created successfully
//            val questionsAdded = withContext(Dispatchers.IO) { addQuestionsToBackend() }
//            if (questionsAdded) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Task and questions saved successfully",
//                    Toast.LENGTH_SHORT
//                ).show()
//                finish() // Close the activity if everything succeeded
//            } else {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Failed to save questions",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//
//    private suspend fun addTaskToBackend(): Boolean {
//        val totalMarks = totalMarksEditText.text.toString().toFloatOrNull() ?: 0f
//
//        if (mainActivityId <= 0 || offeredCourseId <= 0 || totalMarks <= 0) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Invalid parameters for task creation",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            return false
//        }
//
//        val request = TaskRequest(
//            typeId = mainActivityId,
//            oc_id = offeredCourseId,
//            tMarks = totalMarks,
//            further_id = if (subActivityId > 0) subActivityId else 0
//        )
//
//        return try {
//            val response = RetrofitInstance.apiService.addTask(request)
//            if (response.isSuccessful) {
//                response.body()?.let {
//                    taskId = it.TaskId
//                    taskIdentifier = it.TaskIdentifier
//                    smallTaskId = it.SmallTaskId
//                    true
//                } ?: false
//            } else {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@TASK_QUESTIONS,
//                        "Failed to create task: ${response.code()}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                false
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Error: ${e.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//            false
//        }
//    }
//
//    private suspend fun addQuestionsToBackend(): Boolean {
//        if (taskId == -1) return false
//
//        var allSuccess = true
//        var errorMessage: String? = null
//
//        for (i in 0 until questionsContainer.childCount) {
//            val view = questionsContainer.getChildAt(i)
//            val questionEditText = view.findViewById<EditText>(R.id.questionEditText)
//            val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
//
//            val questionText = questionEditText.text.toString()
//            val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f
//
//            if (questionText.isNotEmpty() && marks > 0) {
//                val request = QuestionRequest(
//                    tsk_id = taskId,
//                    que = questionText,
//                    tMarks = marks
//                )
//
//                try {
//                    val response = RetrofitInstance.apiService.addQuestion(request)
//                    if (!response.isSuccessful || response.body()?.Success != true) {
//                        allSuccess = false
//                        errorMessage = response.body()?.Message ?: "Failed to add question"
//                        break
//                    }
//                } catch (e: Exception) {
//                    allSuccess = false
//                    errorMessage = e.message
//                    break
//                }
//            }
//        }
//
//        if (!allSuccess && errorMessage != null) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(this@TASK_QUESTIONS, errorMessage, Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        return allSuccess
//    }
//
//    private fun updateQuestions() {
//        CoroutineScope(Dispatchers.Main).launch {
//            val totalMarks = try {
//                totalMarksEditText.text.toString().toFloat()
//            } catch (e: NumberFormatException) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Please enter valid total marks",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            if (questionsContainer.childCount == 0) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Please add at least one question",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            // Collect updated questions and mark deletions
//            val updatedQuestions = mutableListOf<QuestionUpdate>()
//            val deletedQuestionIds = mutableListOf<Int>()
//
//            // First find all existing questions that were deleted
//            val currentQuestionIds = mutableListOf<Int>()
//            for (i in 0 until questionsContainer.childCount) {
//                val view = questionsContainer.getChildAt(i)
//                val questionId = view.tag as? Int ?: 0
//                if (questionId != 0) {
//                    currentQuestionIds.add(questionId)
//                }
//            }
//
//            // Find deleted questions (existing but not in current view)
//            existingQuestions.forEach { question ->
//                if (!currentQuestionIds.contains(question.tq_id)) {
//                    deletedQuestionIds.add(question.tq_id)
//                }
//            }
//
//            // Collect updated/new questions
//            for (i in 0 until questionsContainer.childCount) {
//                val view = questionsContainer.getChildAt(i)
//                val questionEditText = view.findViewById<EditText>(R.id.questionEditText)
//                val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
//                val questionId = view.tag as? Int ?: 0
//
//                val questionText = questionEditText.text.toString()
//                val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f
//
//                if (questionText.isNotEmpty() && marks > 0) {
//                    updatedQuestions.add(QuestionUpdate(questionId, questionText, marks))
//                }
//            }
//
//            // Call update API
//            val success = withContext(Dispatchers.IO) {
//                try {
//                    val request = TaskWithQuestionsUpdateRequest(
//                        TaskTotalMarks = totalMarks,
//                        Questions = updatedQuestions,
//                        DeletedQuestionIds = deletedQuestionIds
//                    )
//
//                    val response =
//                        RetrofitInstance.apiService.updateTaskWithQuestions(taskId, request)
//                    response.isSuccessful && response.body() != null
//                } catch (e: Exception) {
//                    Log.e("TASK_QUESTIONS", "Error updating questions", e)
//                    false
//                }
//            }
//
//            if (success) {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Task updated successfully",
//                    Toast.LENGTH_SHORT
//                ).show()
//                finish()
//            } else {
//                Toast.makeText(
//                    this@TASK_QUESTIONS,
//                    "Failed to update task",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    // --- CLO Mapping Functions (now correctly inside the class) ---
//
//    private fun showCloMappingDialog() {
//        if (taskId == -1) {
//            Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val dialog = Dialog(this)
//        dialog.setContentView(R.layout.dialog_clo_mapping)
//        dialog.setCancelable(true)
//
//        val tableLayout = dialog.findViewById<TableLayout>(R.id.mappingTable)
//        val saveButton = dialog.findViewById<Button>(R.id.saveMappingButton)
//
//        // Fetch and display CLO mappings
//        fetchAndDisplayCloMappings(tableLayout)
//
//        saveButton.setOnClickListener {
//            saveCloMappings(tableLayout)
//            dialog.dismiss()
//        }
//
//        dialog.show()
//        dialog.window?.setLayout(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//    }
//
//    private fun fetchAndDisplayCloMappings(tableLayout: TableLayout) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.apiService.getQuestionCloMappingsByTaskId(taskId)
//                if (response.isSuccessful) {
//                    response.body()?.let { mappingResponse ->
//                        withContext(Dispatchers.Main) {
//                            // Clear existing rows (except header)
//                            tableLayout.removeAllViews()
//
//                            // Create header row
//                            val headerRow = TableRow(this@TASK_QUESTIONS).apply {
//                                addView(TextView(this@TASK_QUESTIONS).apply { // Changed context to this@TASK_QUESTIONS
//                                    text = "Question"
//                                    setPadding(8, 8, 8, 8)
//                                    setTypeface(typeface, Typeface.BOLD)
//                                })
//                            }
//
//                            // Get all unique CLOs
//                            val allClos =
//                                mappingResponse.data.flatMap { it.CLOs.map { clo -> clo.CLO_ID } }
//                                    .distinct()
//                                    .sorted()
//
//                            // Add CLO headers
//                            allClos.forEach { cloId ->
//                                headerRow.addView(TextView(this@TASK_QUESTIONS).apply { // Changed context to this@TASK_QUESTIONS
//                                    text = "CLO $cloId"
//                                    setPadding(8, 8, 8, 8)
//                                    setTypeface(typeface, Typeface.BOLD)
//                                    textAlignment =
//                                        View.TEXT_ALIGNMENT_CENTER // Added for better alignment
//                                })
//                            }
//
//                            tableLayout.addView(headerRow)
//
//                            // Add data rows
//                            mappingResponse.data.forEach { question ->
//                                val row = TableRow(this@TASK_QUESTIONS)
//
//                                // Add question number
//                                row.addView(TextView(this@TASK_QUESTIONS).apply { // Changed context to this@TASK_QUESTIONS
//                                    text = question.QuestionNo
//                                    setPadding(8, 8, 8, 8)
//                                    gravity =
//                                        View.TEXT_ALIGNMENT_CENTER // Added for better alignment
//                                })
//
//                                // Add percentage fields for each CLO
//                                allClos.forEach { cloId ->
//                                    val percentage =
//                                        question.CLOs.firstOrNull { it.CLO_ID == cloId }?.Percentage
//                                            ?: "0"
//
//                                    val editText =
//                                        EditText(this@TASK_QUESTIONS).apply { // Changed context to this@TASK_QUESTIONS
//                                            setText(percentage)
//                                            inputType =
//                                                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL // Added decimal input type
//                                            setPadding(8, 8, 8, 8)
//                                            gravity =
//                                                View.TEXT_ALIGNMENT_CENTER // Added for better alignment
//                                            tag =
//                                                "${question.QuestionId}_$cloId" // Store questionId and CLO_ID
//
//                                            // Add a TextWatcher for real-time validation (optional but good practice)
//                                            addTextChangedListener(object : TextWatcher {
//                                                override fun beforeTextChanged(
//                                                    s: CharSequence?,
//                                                    start: Int,
//                                                    count: Int,
//                                                    after: Int
//                                                ) {
//                                                }
//
//                                                override fun onTextChanged(
//                                                    s: CharSequence?,
//                                                    start: Int,
//                                                    before: Int,
//                                                    count: Int
//                                                ) {
//                                                }
//
//                                                override fun afterTextChanged(s: Editable?) {
//                                                    try {
//                                                        val value = s.toString().toFloat()
//                                                        if (value < 0 || value > 100) {
//                                                            error =
//                                                                "0-100" // Set error if outside range
//                                                        } else {
//                                                            error = null // Clear error
//                                                        }
//                                                    } catch (e: NumberFormatException) {
//                                                        error =
//                                                            "Invalid" // Set error for non-numeric input
//                                                    }
//                                                }
//                                            })
//                                        }
//
//                                    row.addView(editText)
//                                }
//
//                                tableLayout.addView(row)
//                            }
//                        }
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(
//                            this@TASK_QUESTIONS,
//                            "Failed to load CLO mappings: ${response.code()} - ${response.message()}", // Added response code and message for better debugging
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        Log.e(
//                            "TASK_QUESTIONS",
//                            "CLO mapping load failed: ${response.errorBody()?.string()}"
//                        ) // Log error body
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@TASK_QUESTIONS,
//                        "Error fetching CLO mappings: ${e.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.e(
//                        "TASK_QUESTIONS",
//                        "Exception fetching CLO mappings",
//                        e
//                    ) // Log the full exception
//                }
//            }
//        }
//    }
//
//    private fun saveCloMappings(tableLayout: TableLayout) {
//        val updates = mutableListOf<UpdatePercentageRequest>()
//
//        // Collect all updates from the table
//        for (i in 1 until tableLayout.childCount) {
//            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
//            for (j in 1 until row.childCount) {
//                val editText = row.getChildAt(j) as? EditText ?: continue
//                val (questionId, cloId) = editText.tag.toString().split("_").map { it.toInt() }
//                val percentage = editText.text.toString().takeIf { it.isNotEmpty() } ?: "0"
//                updates.add(UpdatePercentageRequest(questionId, cloId, percentage))
//            }
//        }
//
//        if (updates.isNotEmpty()) {
//            CoroutineScope(Dispatchers.IO).launch {
//                var allSuccess = true
//                var errorMessage: String? = null
//
//                // Process updates one by one (since backend only accepts single updates)
//                for (update in updates) {
//                    try {
//                        val response =
//                            RetrofitInstance.apiService.updateCloMappingPercentage(update)
//                        if (!response.isSuccessful || response.body()?.success != true) {
//                            allSuccess = false
//                            errorMessage = response.body()?.message ?: "Failed to update mapping"
//                            break
//                        }
//                    } catch (e: Exception) {
//                        allSuccess = false
//                        errorMessage = e.message ?: "Unknown error occurred"
//                        break
//                    }
//                }
//
//                withContext(Dispatchers.Main) {
//                    if (allSuccess) {
//                        Toast.makeText(
//                            this@TASK_QUESTIONS,
//                            "All CLO mappings updated successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        Toast.makeText(
//                            this@TASK_QUESTIONS,
//                            errorMessage ?: "Failed to update some mappings",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }
//    }
//}


package com.example.obe_mngt_sys.ACTIVITIES

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class TASK_QUESTIONS : AppCompatActivity() {

    private var questionCount = 1
    private lateinit var questionsContainer: LinearLayout
    private lateinit var totalMarksEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var updateButton: Button
    private var taskId: Int = -1
    private var taskIdentifier: String? = null
    private var smallTaskId: Int? = null
    private var isEditMode = false
    private var existingQuestions = mutableListOf<QuestionDetails>()
    private var offeredCourseId: Int = -1
    private var mainActivityId: Int = -1
    private var subActivityId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_task_questions)

        // Initialize views
        questionsContainer = findViewById(R.id.questionsContainer)
        val addQuestionButton = findViewById<Button>(R.id.addQuestionButton)
        saveButton = findViewById(R.id.saveButton)
        updateButton = findViewById(R.id.updateButton)
        totalMarksEditText = findViewById(R.id.totalMarksEditText)

        // Get the passed data
        mainActivityId = intent.getIntExtra("MAIN_ACTIVITY_ID", -1)
        val mainActivityName = intent.getStringExtra("MAIN_ACTIVITY_NAME") ?: ""
        subActivityId = intent.getIntExtra("SUB_ACTIVITY_ID", -1)
        val subActivityName = intent.getStringExtra("SUB_ACTIVITY_NAME") ?: ""
        offeredCourseId = intent.getIntExtra("OFFERED_COURSE_ID", -1)
        taskId = intent.getIntExtra("TSK_ID", -1).takeIf { it != -1 }
            ?: intent.getIntExtra("TASK_ID", -1)

        Log.d("TASK_DEBUG", "Received task ID: $taskId")

        // Check if we're in edit mode
        isEditMode = taskId != -1

        // Update the title
        val textViewTeacherName = findViewById<TextView>(R.id.textViewTeacherName)
        textViewTeacherName.text = if (subActivityId != -1) {
            "$mainActivityName - $subActivityName"
        } else {
            mainActivityName
        }

        // Set up back button
        findViewById<ImageView>(R.id.backIcon).setOnClickListener {
            finish()
        }

        // Setup appropriate mode
        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }

        // Add question button click listener
        addQuestionButton.setOnClickListener {
            addQuestionField()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            saveQuestions()
        }

        // Update button click listener
        updateButton.setOnClickListener {
            updateQuestions()
        }

        // CLO Mapping button
        findViewById<Button>(R.id.mappingButton).setOnClickListener {
            showCloMappingDialog()
        }

        // Add text watcher for total marks validation
        totalMarksEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateTotalMarksInRealTime()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupEditMode() {
        saveButton.visibility = View.GONE
        updateButton.visibility = View.VISIBLE
        loadExistingTaskDetails()
    }

    private fun setupCreateMode() {
        saveButton.visibility = View.VISIBLE
        updateButton.visibility = View.GONE
        addQuestionField() // Add first empty question
    }

    private fun loadExistingTaskDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getTaskDetails(taskId)
                if (response.isSuccessful) {
                    response.body()?.let { taskDetails ->
                        withContext(Dispatchers.Main) {
                            totalMarksEditText.setText(taskDetails.Task.tMarks.toString())
                            existingQuestions.clear()
                            existingQuestions.addAll(taskDetails.Questions)
                            taskDetails.Questions.forEach { question ->
                                addQuestionField(question.tq_id, question.que, question.tMarks)
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TASK_QUESTIONS,
                            "Failed to load task details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TASK_QUESTIONS,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("TASK_QUESTIONS", "Error loading task details", e)
                }
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun addQuestionField(
        questionId: Int = 0,
        questionText: String = "",
        marks: Float = 0f
    ) {
        val questionView = LayoutInflater.from(this).inflate(R.layout.question_item, null)
        val questionNumber = questionCount++
        val questionLabel = questionView.findViewById<TextView>(R.id.questionLabel)






        questionLabel.text = "Question $questionNumber"

        if (isEditMode && questionId != 0) {
            questionView.findViewById<EditText>(R.id.questionEditText).setText(questionText)
            questionView.findViewById<EditText>(R.id.questionMarksEditText)
                .setText(marks.toString())
            questionView.tag = questionId
        }

        val questionEditText = questionView.findViewById<EditText>(R.id.questionEditText)
        val questionMarksEditText = questionView.findViewById<EditText>(R.id.questionMarksEditText)
        val Difficulty_level = questionView.findViewById<EditText>(R.id.dlevel)



        questionMarksEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateQuestionMarks(questionMarksEditText)
                validateTotalMarksInRealTime()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        val deleteButton = questionView.findViewById<ImageButton>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            questionsContainer.removeView(questionView)
            renumberQuestions()
            validateTotalMarksInRealTime()
        }











            //----------------------------------------------------------------------------------











        questionsContainer.addView(questionView)
    }

    private fun validateQuestionMarks(marksEditText: EditText) {
        try {
            val marks = marksEditText.text.toString().toFloat()
            if (marks < 0) {
                marksEditText.error = "Marks cannot be negative"
            } else {
                marksEditText.error = null
            }
        } catch (e: NumberFormatException) {
            marksEditText.error = "Invalid marks"
        }
    }

    private fun validateTotalMarks(): Boolean {
        val totalMarks = try {
            totalMarksEditText.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            return false
        }

        var sumOfQuestionMarks = 0f
        for (i in 0 until questionsContainer.childCount) {
            val view = questionsContainer.getChildAt(i)
            val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
            val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f
            sumOfQuestionMarks += marks
        }

        return sumOfQuestionMarks <= totalMarks
    }

    private fun validateTotalMarksInRealTime() {
        val totalMarks = try {
            totalMarksEditText.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            return
        }

        var sumOfQuestionMarks = 0f
        for (i in 0 until questionsContainer.childCount) {
            val view = questionsContainer.getChildAt(i)
            val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
            val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f
            sumOfQuestionMarks += marks
        }

        if (sumOfQuestionMarks > totalMarks) {
            for (i in 0 until questionsContainer.childCount) {
                val view = questionsContainer.getChildAt(i)
                val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
                questionMarksEditText.error = "Total exceeded!"
            }
            totalMarksEditText.error = "Sum of questions ($sumOfQuestionMarks) exceeds total marks ($totalMarks)"
        } else {
            for (i in 0 until questionsContainer.childCount) {
                val view = questionsContainer.getChildAt(i)
                val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
                questionMarksEditText.error = null
            }
            totalMarksEditText.error = null
        }
    }

    private fun renumberQuestions() {
        questionCount = 1
        for (i in 0 until questionsContainer.childCount) {
            val view = questionsContainer.getChildAt(i)
            val questionLabel = view.findViewById<TextView>(R.id.questionLabel)
            questionLabel.text = "Question ${questionCount++}"
        }
        questionCount = questionsContainer.childCount + 1
    }

    private fun saveQuestions() {
        CoroutineScope(Dispatchers.Main).launch {
            val totalMarks = try {
                totalMarksEditText.text.toString().toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Please enter valid total marks",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (questionsContainer.childCount == 0) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Please add at least one question",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (!validateTotalMarks()) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Sum of question marks exceeds total marks!",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Step 1: Add the task and get ALL task IDs
            val taskIds = withContext(Dispatchers.IO) { addTaskToBackend() }
            if (taskIds.isEmpty()) {
                return@launch
            }

            // Step 2: Add questions to ALL tasks
            val questionsAdded = withContext(Dispatchers.IO) { addQuestionsToBackend(taskIds) }
            if (questionsAdded) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    if (taskIds.size > 1) "Tasks and questions saved successfully for all sections"
                    else "Task and questions saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }




    private suspend fun addTaskToBackend(): List<Int> {
        val totalMarks = totalMarksEditText.text.toString().toFloatOrNull() ?: 0f

        if (mainActivityId <= 0 || offeredCourseId <= 0 || totalMarks <= 0) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Invalid parameters for task creation",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return emptyList()
        }

        val request = TaskRequest(
            typeId = mainActivityId,
            oc_id = offeredCourseId,
            tMarks = totalMarks,
            further_id = if (subActivityId > 0) subActivityId else 0
        )

        return try {
            val response = RetrofitInstance.apiService.addTask(request)
            if (response.isSuccessful) {
                response.body()?.let { taskResponse ->
                    // Store the first task ID as the primary one
                    taskId = taskResponse.TaskIds?.firstOrNull() ?: -1
                    taskIdentifier = taskResponse.TaskIdentifiers?.firstOrNull()
                    smallTaskId = taskResponse.SmallTaskId

                    // Return ALL task IDs
                    taskResponse.TaskIds
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TASK_QUESTIONS,
                            "Empty response from server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    emptyList()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TASK_QUESTIONS,
                        "Failed to create task: ${response.code()} - ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                emptyList()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            emptyList()
        }
    }
    private suspend fun addQuestionsToBackend(taskIds: List<Int>): Boolean {
        if (taskIds.isEmpty()) return false

        var allSuccess = true
        var errorMessage: String? = null

        // Add questions to ALL task IDs
        for (currentTaskId in taskIds) {
            for (i in 0 until questionsContainer.childCount) {
                val view = questionsContainer.getChildAt(i)
                val questionEditText = view.findViewById<EditText>(R.id.questionEditText)
                val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
                val dificultyLevel=view.findViewById<EditText>(R.id.dlevel)

                val questionText = questionEditText.text.toString()
                val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f
                val deflevel = dificultyLevel.text.toString()

                if (questionText.isNotEmpty() && marks > 0) {
                    val request = QuestionRequest(
                        tsk_id = currentTaskId,  // Use current task ID
                        que = questionText,
                        tMarks = marks,
                        dlevel = deflevel
                    )




                    try {
                        val response = RetrofitInstance.apiService.addQuestion(request)
                        if (!response.isSuccessful) {
                            allSuccess = false
                            errorMessage = "Failed to add question to task $currentTaskId: ${response.code()}"
                            break
                        }

                        response.body()?.let { questionResponse ->
                            if (!questionResponse.Success) {
                                allSuccess = false
                                errorMessage = questionResponse.Message ?: "Failed to add question to task $currentTaskId"
                            }
                        } ?: run {
                            allSuccess = false
                            errorMessage = "Empty response when adding to task $currentTaskId"
                        }
                    } catch (e: Exception) {
                        allSuccess = false
                        errorMessage = "Error adding to task $currentTaskId: ${e.message}"
                        Log.e("TASK_QUESTIONS", "Error updating questions", e)
                        break
                    }
                }
            }
            if (!allSuccess) break
        }

        if (!allSuccess && errorMessage != null) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@TASK_QUESTIONS, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        return allSuccess
    }

    private fun updateQuestions() {
        CoroutineScope(Dispatchers.Main).launch {
            val totalMarks = try {
                totalMarksEditText.text.toString().toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Please enter valid total marks",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            if (questionsContainer.childCount == 0) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Please add at least one question",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Validate total marks
            if (!validateTotalMarks()) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Sum of question marks exceeds total marks!",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Collect updated questions and mark deletions
            val updatedQuestions = mutableListOf<QuestionUpdate>()
            val deletedQuestionIds = mutableListOf<Int>()

            // First find all existing questions that were deleted
            val currentQuestionIds = mutableListOf<Int>()
            for (i in 0 until questionsContainer.childCount) {
                val view = questionsContainer.getChildAt(i)
                val questionId = view.tag as? Int ?: 0
                if (questionId != 0) {
                    currentQuestionIds.add(questionId)
                }
            }

            // Find deleted questions (existing but not in current view)
            existingQuestions.forEach { question ->
                if (!currentQuestionIds.contains(question.tq_id)) {
                    deletedQuestionIds.add(question.tq_id)
                }
            }

            // Collect updated/new questions
            for (i in 0 until questionsContainer.childCount) {
                val view = questionsContainer.getChildAt(i)
                val questionEditText = view.findViewById<EditText>(R.id.questionEditText)
                val questionMarksEditText = view.findViewById<EditText>(R.id.questionMarksEditText)
                val questionId = view.tag as? Int ?: 0

                val questionText = questionEditText.text.toString()
                val marks = questionMarksEditText.text.toString().toFloatOrNull() ?: 0f

                if (questionText.isNotEmpty() && marks > 0) {
                    updatedQuestions.add(QuestionUpdate(questionId, questionText, marks))
                }
            }

            // Call update API
            val success = withContext(Dispatchers.IO) {
                try {
                    val request = TaskWithQuestionsUpdateRequest(
                        TaskTotalMarks = totalMarks,
                        Questions = updatedQuestions,
                        DeletedQuestionIds = deletedQuestionIds
                    )

                    val response =
                        RetrofitInstance.apiService.updateTaskWithQuestions(taskId, request)
                    response.isSuccessful && response.body() != null
                } catch (e: Exception) {
                    Log.e("TASK_QUESTIONS", "Error updating questions", e)
                    false
                }
            }

            if (success) {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Task updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this@TASK_QUESTIONS,
                    "Failed to update task",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showCloMappingDialog() {
        if (taskId == -1) {
            Toast.makeText(this, "Please save the task first", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_clo_mapping)
        dialog.setCancelable(true)

        val tableLayout = dialog.findViewById<TableLayout>(R.id.mappingTable)
        val saveButton = dialog.findViewById<Button>(R.id.saveMappingButton)

        fetchAndDisplayCloMappings(tableLayout)

        saveButton.setOnClickListener {
            saveCloMappings(tableLayout)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun fetchAndDisplayCloMappings(tableLayout: TableLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getQuestionCloMappingsByTaskId(taskId)
                if (response.isSuccessful) {
                    response.body()?.let { mappingResponse ->
                        withContext(Dispatchers.Main) {
                            tableLayout.removeAllViews()

                            val headerRow = TableRow(this@TASK_QUESTIONS).apply {
                                addView(TextView(this@TASK_QUESTIONS).apply {
                                    text = "Question"
                                    setPadding(8, 8, 8, 8)
                                    setTypeface(typeface, Typeface.BOLD)
                                })
                            }

                            val allClos =
                                mappingResponse.data.flatMap { it.CLOs.map { clo -> clo.CLO_ID } }
                                    .distinct()
                                    .sorted()

                            allClos.forEach { cloId ->
                                headerRow.addView(TextView(this@TASK_QUESTIONS).apply {
                                    text = "CLO $cloId"
                                    setPadding(8, 8, 8, 8)
                                    setTypeface(typeface, Typeface.BOLD)
                                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                                })
                            }

                            tableLayout.addView(headerRow)

                            mappingResponse.data.forEach { question ->
                                val row = TableRow(this@TASK_QUESTIONS)

                                row.addView(TextView(this@TASK_QUESTIONS).apply {
                                    text = question.QuestionNo
                                    setPadding(8, 8, 8, 8)
                                    gravity = View.TEXT_ALIGNMENT_CENTER
                                })

                                allClos.forEach { cloId ->
                                    val percentage =
                                        question.CLOs.firstOrNull { it.CLO_ID == cloId }?.Percentage
                                            ?: "0"

                                    val editText =
                                        EditText(this@TASK_QUESTIONS).apply {
                                            setText(percentage)
                                            inputType =
                                                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                                            setPadding(8, 8, 8, 8)
                                            gravity = View.TEXT_ALIGNMENT_CENTER
                                            tag = "${question.QuestionId}_$cloId"

                                            addTextChangedListener(object : TextWatcher {
                                                override fun beforeTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    count: Int,
                                                    after: Int
                                                ) {
                                                }

                                                override fun onTextChanged(
                                                    s: CharSequence?,
                                                    start: Int,
                                                    before: Int,
                                                    count: Int
                                                ) {
                                                }

                                                override fun afterTextChanged(s: Editable?) {
                                                    try {
                                                        val value = s.toString().toFloat()
                                                        if (value < 0 || value > 100) {
                                                            error = "0-100"
                                                        } else {
                                                            error = null
                                                        }
                                                    } catch (e: NumberFormatException) {
                                                        error = "Invalid"
                                                    }
                                                }
                                            })
                                        }

                                    row.addView(editText)
                                }

                                tableLayout.addView(row)
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@TASK_QUESTIONS,
                            "Failed to load CLO mappings: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "TASK_QUESTIONS",
                            "CLO mapping load failed: ${response.errorBody()?.string()}"
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@TASK_QUESTIONS,
                        "Error fetching CLO mappings: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "TASK_QUESTIONS",
                        "Exception fetching CLO mappings",
                        e
                    )
                }
            }
        }
    }

    private fun saveCloMappings(tableLayout: TableLayout) {
        val updates = mutableListOf<UpdatePercentageRequest>()

        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as? TableRow ?: continue
            for (j in 1 until row.childCount) {
                val editText = row.getChildAt(j) as? EditText ?: continue
                val (questionId, cloId) = editText.tag.toString().split("_").map { it.toInt() }
                val percentage = editText.text.toString().takeIf { it.isNotEmpty() } ?: "0"
                updates.add(UpdatePercentageRequest(questionId, cloId, percentage))
            }
        }

        if (updates.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                var allSuccess = true
                var errorMessage: String? = null

                for (update in updates) {
                    try {
                        val response =
                            RetrofitInstance.apiService.updateCloMappingPercentage(update)
                        if (!response.isSuccessful || response.body()?.success != true) {
                            allSuccess = false
                            errorMessage = response.body()?.message ?: "Failed to update mapping"
                            break
                        }
                    } catch (e: Exception) {
                        allSuccess = false
                        errorMessage = e.message ?: "Unknown error occurred"
                        break
                    }
                }

                withContext(Dispatchers.Main) {
                    if (allSuccess) {
                        Toast.makeText(
                            this@TASK_QUESTIONS,
                            "All CLO mappings updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@TASK_QUESTIONS,
                            errorMessage ?: "Failed to update some mappings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}