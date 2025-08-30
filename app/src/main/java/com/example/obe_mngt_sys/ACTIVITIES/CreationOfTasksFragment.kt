package com.example.obe_mngt_sys.ACTIVITIES

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.Activity
import com.example.obe_mngt_sys.MODELS.CourseTask
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CreationOfTasksFragment : Fragment() {
    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var semester: Int? = null
    private var offeredCourseId: Int = -1
    private lateinit var tableHeader: TableLayout
    private lateinit var tableContent: TableLayout
    private lateinit var textViewCourseName: TextView
    private lateinit var textViewSection: TextView
    private lateinit var textViewSemester: TextView
    private lateinit var spinnerTaskType: Spinner
    private lateinit var spinnerSubTaskType: Spinner
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutSubType: LinearLayout
    private lateinit var buttonProceed: Button
    private var teacherId: String = ""
    private var selectedMainActivity: Activity? = null
    private var selectedSubActivity: Activity? = null
    private var activityMap: Map<String, Activity> = emptyMap()
    private var subtypeMap: Map<String, Activity> = emptyMap()
    private var hasMidFinalPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseCode = it.getString("courseCode")
            courseName = it.getString("courseName")
            section = it.getString("section")
            semester = it.getInt("semester")
            offeredCourseId = it.getInt("offeredCourseId", -1)
            teacherId = it.getString("teacherId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_creation_of_tasks, container, false)
        initializeViews(view)
        setupCourseInfo()
        setupTaskTypeSpinner()
        setupProceedButton()
        loadTasksIfAvailable()
        return view
    }

    private fun initializeViews(view: View) {
        tableHeader = view.findViewById(R.id.tableHeader)
        tableContent = view.findViewById(R.id.tableContent)
        textViewCourseName = view.findViewById(R.id.textViewCourseName)
        textViewSection = view.findViewById(R.id.textViewSection)
        textViewSemester = view.findViewById(R.id.textViewSemester)
        spinnerTaskType = view.findViewById(R.id.spinnerTaskType)
        spinnerSubTaskType = view.findViewById(R.id.spinnerSubTaskType)
        layoutSubType = view.findViewById(R.id.layoutSubType)
        progressBar = view.findViewById(R.id.progressBar)
        buttonProceed = view.findViewById(R.id.buttonProceed)
    }

    private fun setupCourseInfo() {
        textViewCourseName.text = "Course: $courseName ($courseCode)"
        textViewSection.text = "Section: $section"
        textViewSemester.text = "Semester: $semester"
    }

    private fun setupProceedButton() {
        buttonProceed.setOnClickListener {
            proceedToNextActivity()
        }
    }

    private fun loadTasksIfAvailable() {
        if (offeredCourseId != -1) {
            loadTasks(offeredCourseId)
        }
    }

    private fun setupTaskTypeSpinner() {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check permission first
                val permissionResponse = RetrofitInstance.apiService.CheckPermission(offeredCourseId, teacherId)
                hasMidFinalPermission = permissionResponse.isSuccessful &&
                        permissionResponse.body()?.Result == true

                // Get activities based on permission
                val response = RetrofitInstance.apiService.getMainActivities(offeredCourseId, teacherId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        response.body()?.let { activities ->
                            if (activities.isNotEmpty()) {
                                setupMainSpinner(activities)
                            } else {
                                showMessage("No main activities found")
                                setupFallbackSpinner()
                            }
                        } ?: showMessage("Empty response from server")
                    } else {
                        showMessage("Failed to load main activities: ${response.code()}")
                        setupFallbackSpinner()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    showMessage("Error: ${e.message}")
                    setupFallbackSpinner()
                }
            }
        }
    }

    private fun setupMainSpinner(activities: List<Activity>) {
        val items = mutableListOf("SELECT").apply {
            addAll(activities.map { it.Typee })
        }

        val mainAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(Color.GRAY)
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                return view
            }
        }

        mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskType.adapter = mainAdapter
        spinnerTaskType.setSelection(0)

        activityMap = activities.associateBy { it.Typee }

        spinnerTaskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
                    val selectedTask = parent.getItemAtPosition(position).toString()
                    selectedMainActivity = activityMap[selectedTask]
                    selectedSubActivity = null
                    spinnerSubTaskType.setSelection(0)

                    selectedMainActivity?.let {
                        if (it.typeId == 1 || it.typeId == 4) {
                            loadSubtypes(it.typeId)
                        } else {
                            layoutSubType.visibility = View.GONE
                        }
                    }
                } else {
                    layoutSubType.visibility = View.GONE
                    selectedMainActivity = null
                    selectedSubActivity = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                layoutSubType.visibility = View.GONE
                selectedMainActivity = null
                selectedSubActivity = null
            }
        }
    }

    private fun loadSubtypes(mainActivityId: Int) {
        progressBar.visibility = View.VISIBLE
        layoutSubType.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getSubtypes(mainActivityId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        response.body()?.let { subtypes ->
                            if (subtypes.isNotEmpty()) {
                                setupSubtypeSpinner(subtypes)
                                layoutSubType.visibility = View.VISIBLE
                            } else {
                                showToast("No subtypes available")
                            }
                        } ?: showToast("Invalid response format")
                    } else {
                        showToast("Error ${response.code()}: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    showToast("Network error: ${e.message}")
                }
            }
        }
    }

    private fun setupSubtypeSpinner(subtypes: List<Activity>) {
        subtypeMap = subtypes.associateBy { it.Typee }

        val items = mutableListOf("SELECT").apply {
            addAll(subtypes.map { it.Typee })
        }

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(Color.GRAY)
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubTaskType.adapter = adapter
        spinnerSubTaskType.setSelection(0)

        spinnerSubTaskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position > 0) {
                    val selectedSubTask = parent.getItemAtPosition(position).toString()
                    selectedSubActivity = subtypeMap[selectedSubTask]
                } else {
                    selectedSubActivity = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedSubActivity = null
            }
        }
    }

    private fun setupFallbackSpinner() {
        val fallbackActivities = mutableListOf(
            Activity(1, "HW"),
            Activity(4, "PRACTICAL")
        )

        if (hasMidFinalPermission) {
            fallbackActivities.addAll(listOf(
                Activity(2, "MID"),
                Activity(3, "FINAL")
            ))
        }

        val items = mutableListOf("SELECT").apply {
            addAll(fallbackActivities.map { it.Typee })
        }

        val mainAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(Color.GRAY)
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                return view
            }
        }

        mainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskType.adapter = mainAdapter
        spinnerTaskType.setSelection(0)
    }

    private fun proceedToNextActivity() {
        selectedMainActivity?.let { mainActivity ->
            if ((mainActivity.typeId == 1 || mainActivity.typeId == 4) && selectedSubActivity == null) {
                showToast("Please select a subtype")
                return
            }

            val intent = Intent(requireContext(), TASK_QUESTIONS::class.java).apply {
                putExtra("MAIN_ACTIVITY_ID", mainActivity.typeId)
                putExtra("MAIN_ACTIVITY_NAME", mainActivity.Typee)
                selectedSubActivity?.let { subActivity ->
                    putExtra("SUB_ACTIVITY_ID", subActivity.typeId)
                    putExtra("SUB_ACTIVITY_NAME", subActivity.Typee)
                }
                putExtra("OFFERED_COURSE_ID", offeredCourseId)
                putExtra("COURSE_CODE", courseCode)
                putExtra("COURSE_NAME", courseName)
                putExtra("SECTION", section)
                putExtra("SEMESTER", semester)
                putExtra("TEACHER_ID", teacherId)
            }

            startActivity(intent)
        } ?: run {
            showToast("Please select a task type")
        }
    }

    private fun loadTasks(courseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getTasksByOfferID(courseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { tasks ->
                            if (tasks.isNotEmpty()) {
                                showTasks(tasks)
                            } else {
                                showMessage("No tasks found")
                            }
                        }
                    } else {
                        showMessage("Error loading tasks: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showMessage("Error: ${e.message}")
                }
            }
        }
    }

    private fun showTasks(tasks: List<CourseTask>) {
        tableContent.removeAllViews()

        val headerRow = TableRow(requireContext()).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }
        }

        listOf("Type", "Task ID", "Add", "Edit", "Delete").forEach { headerText ->
            headerRow.addView(TextView(requireContext()).apply {
                text = headerText
                setTypeface(null, Typeface.BOLD)
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.table_header_bg))
                gravity = Gravity.CENTER
            })
        }
        tableContent.addView(headerRow)

        for (task in tasks) {
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.addView(TextView(requireContext()).apply {
                text = task.Typee
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                gravity = Gravity.CENTER
            })

            row.addView(TextView(requireContext()).apply {
                text = task.SmallTask_ID ?: task.Task_ID
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
                gravity = Gravity.CENTER
            })

            row.addView(createActionIcon(R.drawable.ic_add, Color.BLUE) { onAddClicked(task) })
            row.addView(createActionIcon(R.drawable.ic_edit, Color.GREEN) { onEditClicked(task) })
            row.addView(createActionIcon(R.drawable.ic_delete, Color.RED) { onDeleteClicked(task) })

            tableContent.addView(row)
        }
    }

    private fun createActionIcon(resId: Int, color: Int, onClick: () -> Unit): ImageView {
        return ImageView(requireContext()).apply {
            setImageResource(resId)
            contentDescription = resources.getResourceEntryName(resId)
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
            setOnClickListener { onClick() }
            setColorFilter(color)
        }
    }

    private fun onEditClicked(task: CourseTask) {
        val intent = Intent(requireContext(), TASK_QUESTIONS::class.java).apply {
            putExtra("TSK_ID", task.Tsk_ID)
            putExtra("TASK_ID", task.Task_ID)
            putExtra("MAIN_ACTIVITY_ID", task.Type_ID)
            putExtra("MAIN_ACTIVITY_NAME", task.Typee)
            putExtra("OFFERED_COURSE_ID", offeredCourseId)
            putExtra("COURSE_CODE", courseCode)
            putExtra("COURSE_NAME", courseName)
            putExtra("SECTION", section)
            putExtra("SEMESTER", semester)
            putExtra("TEACHER_ID", teacherId)
        }
        startActivity(intent)
    }

    private fun onAddClicked(task: CourseTask) {
        val fragment = AddResultFragment.newInstance(
            courseCode ?: "",
            courseName ?: "",
            section ?: "",
            semester ?: 0,
            offeredCourseId,
            task.Tsk_ID
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun onDeleteClicked(task: CourseTask) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this task and all its questions?")
            .setPositiveButton("Delete") { dialog, which ->
                deleteTask(task.Tsk_ID)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask(taskId: Int) {
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.deleteTask(taskId)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        showToast("Task deleted successfully")
                        loadTasks(offeredCourseId)
                    } else {
                        showToast("Error deleting task: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessage(message: String) {
        tableContent.removeAllViews()
        val row = TableRow(requireContext())
        val msgView = TextView(requireContext()).apply {
            text = message
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        }
        row.addView(msgView)
        tableContent.addView(row)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            teacherId: String
        ): CreationOfTasksFragment {
            return CreationOfTasksFragment().apply {
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