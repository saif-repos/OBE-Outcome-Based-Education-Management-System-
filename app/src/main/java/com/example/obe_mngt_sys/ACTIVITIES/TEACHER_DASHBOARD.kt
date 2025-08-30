//package com.example.obe_mngt_sys.ACTIVITIES
//
//import android.util.Log
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Switch
//
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.obe_mngt_sys.ADAPTERS.Teacher_Courses_Adapter
//import com.example.obe_mngt_sys.HELPER.RetrofitInstance
//import com.example.obe_mngt_sys.MODELS.teacher_Courses
//import com.example.obe_mngt_sys.R
//import com.example.obe_mngt_sys.databinding.ActivityTeacherDashboard2Binding
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//class TEACHER_DASHBOARD : AppCompatActivity() {
//    private lateinit var binding: ActivityTeacherDashboard2Binding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityTeacherDashboard2Binding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Get teacherId and teacherName from intent
//        val teacherId = intent.getStringExtra("UserID") ?: ""
//        val teacherName = intent.getStringExtra("UserName") ?: ""
//
//        // Set teacher name in the TextView
//        val textViewTeacherName = findViewById<TextView>(R.id.textViewTeacherName)
//        textViewTeacherName.text = teacherName
//
//        // 🔹 Back Icon Functionality
//        binding.backIcon.setOnClickListener {
//            val intent = Intent(this@TEACHER_DASHBOARD, Login::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            finish()
//        }
//
//        // In TEACHER_DASHBOARD's onCreate
//        binding.bellIcon.isEnabled = false
//        binding.bellIcon.alpha = 0.5f // Make it appear disabled
//
//
//
//
//        // Disable bell icon
//        binding.bellIcon.isEnabled = false
//        binding.bellIcon.alpha = 0.5f
//
//        // Toggle switch functionality
//        val roleSwitch = findViewById<Switch>(R.id.roleSwitch)
//        roleSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                // Teacher mode - stay in current activity
//                fetchTeacherCourses(teacherId)
//            } else {
//                // Advisor mode - go to Advisor section
//                val intent = Intent(this@TEACHER_DASHBOARD, Advisor_section::class.java)
//                intent.putExtra("UserID", teacherId)
//                intent.putExtra("UserName", teacherName)
//                startActivity(intent)
//            }
//        }
//
//
//        // Fetch teacher's courses from the API
//        fetchTeacherCourses(teacherId)
//    }
//
//    // Modify fetchTeacherCourses to pass teacherId to setupRecyclerView
//    private fun fetchTeacherCourses(teacherId: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitInstance.apiService.TeacherDashboard(teacherId)
//                if (response.isSuccessful) {
//                    val dashboardResponse = response.body()
//                    if (dashboardResponse != null) {
//                        Log.d("API_DEBUG", "Raw API response: ${response.body()}")
//
//                        val courses = dashboardResponse.Courses.map { apiCourse ->
//                            Log.d("API_DEBUG", "Mapping course: ${apiCourse.OfferedCourseId}")
//                            val section = apiCourse.SectionWithSemester
//                            val semester = section.substring(0, 1).toIntOrNull() ?: 1
//
//                            teacher_Courses(
//                                offeredCourseId = apiCourse.OfferedCourseId,
//                                courseCode = apiCourse.CourseCode,
//                                courseName = apiCourse.CourseName,
//                                section = section,
//                                semester = semester
//                            )
//                        }.toMutableList()
//
//                        withContext(Dispatchers.Main) {
//                            setupRecyclerView(courses, dashboardResponse.TeacherName, teacherId)
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                // Error handling
//            }
//        }
//    }
//
//
//    // Helper function to extract semester from SectionWithSemester
//    private fun extractSemesterFromSection(sectionWithSemester: String): Int {
//        // Example: "1A" -> semester = 1
//        return sectionWithSemester.substring(0, 1).toIntOrNull()
//            ?: 1 // Default to 1 if parsing fails
//    }
//
//
//    private fun setupRecyclerView(
//        courses: MutableList<teacher_Courses>,
//        teacherName: String, // Add teacherName parameter
//        teacherId: String // Add teacherId parameter
//    ) {
//        val recyclerView = findViewById<RecyclerView>(R.id.courseRecyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        // Pass both courses and teacherName to the adapter
//        val adapter = Teacher_Courses_Adapter(courses, teacherName, teacherId)
//        recyclerView.adapter = adapter
//    }
//}
//
package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.Teacher_Courses_Adapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.AvailableSessionsResponse
import com.example.obe_mngt_sys.MODELS.SessionYearOption
import com.example.obe_mngt_sys.MODELS.TeacherCoursesBySessionResponse
import com.example.obe_mngt_sys.MODELS.TeacherDashboardResponse
import com.example.obe_mngt_sys.MODELS.teacher_Courses // Your local model for RecyclerView
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.ActivityTeacherDashboard2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TEACHER_DASHBOARD : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherDashboard2Binding
    private lateinit var teacherId: String
    private lateinit var teacherName: String

    // Store available sessions for the spinner
    private var availableSessions: List<SessionYearOption> = emptyList()

    // Variable to prevent immediate spinner selection callback on initial population
    private var isSpinnerInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTeacherDashboard2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get teacherId and teacherName from intent
        teacherId = intent.getStringExtra("UserID") ?: ""
        teacherName = intent.getStringExtra("UserName") ?: ""

        // Set teacher name in the TextView
        binding.textViewTeacherName.text = teacherName

        // 🔹 Back Icon Functionality
        binding.backIcon.setOnClickListener {
            val intent = Intent(this@TEACHER_DASHBOARD, Login::class.java)
            // Clear task stack to prevent going back to dashboard from login
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Disable bell icon (Suggestions)
        binding.bellIcon.isEnabled = false
        binding.bellIcon.alpha = 0.5f // Make it appear disabled

        // Toggle switch functionality
        binding.roleSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Teacher mode - stay in current activity and re-fetch dashboard
                Toast.makeText(this, "Switched to Teacher Mode", Toast.LENGTH_SHORT).show()
                fetchTeacherDashboardCourses(teacherId)
            } else {
                // Advisor mode - go to Advisor section
                Toast.makeText(this, "Switched to Advisor Mode", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@TEACHER_DASHBOARD, Advisor_section::class.java)
                intent.putExtra("UserID", teacherId)
                intent.putExtra("UserName", teacherName)
                startActivity(intent)
                finish() // Finish current activity when switching to advisor
            }
        }

        // Initialize the spinner with an empty adapter initially
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sessionSpinner.adapter = spinnerAdapter

        // Set up spinner item selection listener
        binding.sessionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Prevent callback from triggering on initial population
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }

                if (position >= 0 && position < availableSessions.size) {
                    val selectedOption = availableSessions[position]
                    Log.d("SpinnerSelection", "Selected: Session=${selectedOption.Session}, Year=${selectedOption.Year}")
                    fetchTeacherCoursesBySession(teacherId, selectedOption.Session, selectedOption.Year)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Initial fetch for dashboard and available sessions
        // Start by fetching dashboard courses (which implicitly determines the "current" session)
        // and then fetch all available sessions to populate the spinner.
        fetchTeacherDashboardCourses(teacherId)
    }

    // Fetches initial dashboard data (most recent courses) and populates the spinner
    private fun fetchTeacherDashboardCourses(teacherId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.TeacherDashboard(teacherId)
                if (response.isSuccessful) {
                    val dashboardResponse = response.body()
                    if (dashboardResponse != null) {
                        Log.d("API_DEBUG", "TeacherDashboard Raw API response: ${response.body()}")

                        val courses = dashboardResponse.Courses.map { apiCourse ->
                            teacher_Courses(
                                offeredCourseId = apiCourse.OfferedCourseId,
                                courseCode = apiCourse.CourseCode,
                                courseName = apiCourse.CourseName,
                                section = apiCourse.SectionWithSemester,
                                semester = extractSemesterFromSection(apiCourse.SectionWithSemester)
                            )
                        }.toMutableList()

                        withContext(Dispatchers.Main) {
                            setupRecyclerView(courses)
                            // Now fetch available sessions for the spinner
                            fetchAvailableSessions(teacherId)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TEACHER_DASHBOARD, "No dashboard data found.", Toast.LENGTH_SHORT).show()
                            setupRecyclerView(mutableListOf()) // Clear courses if no data
                            fetchAvailableSessions(teacherId) // Still try to get sessions even if no courses initially
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Toast.makeText(this@TEACHER_DASHBOARD, "Failed to load dashboard: $errorBody", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR", "TeacherDashboard failed: ${response.code()} - $errorBody")
                        setupRecyclerView(mutableListOf()) // Clear courses on failure
                        fetchAvailableSessions(teacherId) // Still try to get sessions
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TEACHER_DASHBOARD, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("API_ERROR", "Exception fetching dashboard: ${e.message}", e)
                    setupRecyclerView(mutableListOf()) // Clear courses on error
                }
            }
        }
    }

    // Fetches all available session/year combinations for the spinner
    private fun fetchAvailableSessions(teacherId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetAvailableSessions(teacherId)
                if (response.isSuccessful) {
                    val sessionsResponse = response.body()
                    if (sessionsResponse != null) {
                        availableSessions = sessionsResponse.AvailableSessions
                        withContext(Dispatchers.Main) {
                            populateSessionSpinner()

                            // Auto-select the session corresponding to the initially loaded courses
                            // from TeacherDashboard.
                            val initialCourseSession = if (binding.courseRecyclerView.adapter?.itemCount ?: 0 > 0) {
                                // Assuming the first course in the adapter represents the initial session
                                (binding.courseRecyclerView.adapter as? Teacher_Courses_Adapter)?.courses?.firstOrNull()?.let {
                                    "${it.section.split("_")[0]} ${it.section.split("_")[1].substring(0,1).toIntOrNull()}" // This logic needs to match how you parse the initial session
                                }
                            } else null

                            // A more reliable way is to determine the 'targetSession' and 'maxYear'
                            // from the TeacherDashboard API response directly.
                            // For simplicity, let's try to match the first loaded course's session/year
                            // to select it in the spinner.
                            val initialCourses = (binding.courseRecyclerView.adapter as? Teacher_Courses_Adapter)?.courses
                            if (!initialCourses.isNullOrEmpty()) {
                                val firstCourse = initialCourses.first()
                                val targetSessionYearString = "${firstCourse.section.split("_")[0]} ${firstCourse.semester}" // This might need adjustment based on your SectionWithSemester string format from the backend
                                Log.d("SpinnerSelection", "Attempting to pre-select: $targetSessionYearString")

                                val selectedIndex = availableSessions.indexOfFirst { sessionOption ->
                                    "${sessionOption.Session} ${sessionOption.Year}" == targetSessionYearString
                                }

                                if (selectedIndex != -1) {
                                    binding.sessionSpinner.setSelection(selectedIndex)
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TEACHER_DASHBOARD, "No available sessions found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Toast.makeText(this@TEACHER_DASHBOARD, "Failed to load available sessions: $errorBody", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR", "GetAvailableSessions failed: ${response.code()} - $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TEACHER_DASHBOARD, "Network error fetching sessions: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("API_ERROR", "Exception fetching sessions: ${e.message}", e)
                }
            }
        }
    }


    // Populates the spinner with fetched session/year options
    private fun populateSessionSpinner() {
        // Create user-friendly strings for spinner, e.g., "Fall 2023"
        val spinnerItems = availableSessions.map { "${it.Session} ${it.Year}" }.toMutableList()
        val adapter = binding.sessionSpinner.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(spinnerItems)
        adapter.notifyDataSetChanged()
        isSpinnerInitialized = false // Reset flag after populating
    }


    // Fetches courses based on selected session and year from the spinner
    private fun fetchTeacherCoursesBySession(teacherId: String, session: String, year: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetTeacherCoursesBySession(teacherId, session, year)
                if (response.isSuccessful) {
                    val coursesBySessionResponse = response.body()
                    if (coursesBySessionResponse != null) {
                        Log.d("API_DEBUG", "GetTeacherCoursesBySession Raw API response: ${response.body()}")

                        val courses = coursesBySessionResponse.Courses.map { apiCourse ->
                            teacher_Courses(
                                offeredCourseId = apiCourse.OfferedCourseId,
                                courseCode = apiCourse.CourseCode,
                                courseName = apiCourse.CourseName,
                                section = apiCourse.SectionWithSemester,
                                semester = extractSemesterFromSection(apiCourse.SectionWithSemester)
                            )
                        }.toMutableList()

                        withContext(Dispatchers.Main) {
                            setupRecyclerView(courses)
                            // Optionally, update availableSessions here too if GetTeacherCoursesBySession
                            // guarantees the most up-to-date session list.
                            // availableSessions = coursesBySessionResponse.AvailableSessions
                            // populateSessionSpinner()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@TEACHER_DASHBOARD, "No courses found for selected session.", Toast.LENGTH_SHORT).show()
                            setupRecyclerView(mutableListOf()) // Clear courses if no data
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Toast.makeText(this@TEACHER_DASHBOARD, "Failed to load courses for session: $errorBody", Toast.LENGTH_LONG).show()
                        Log.e("API_ERROR", "GetTeacherCoursesBySession failed: ${response.code()} - $errorBody")
                        setupRecyclerView(mutableListOf()) // Clear courses on failure
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TEACHER_DASHBOARD, "Network error fetching courses by session: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("API_ERROR", "Exception fetching courses by session: ${e.message}", e)
                    setupRecyclerView(mutableListOf()) // Clear courses on error
                }
            }
        }
    }


    // Helper function to extract semester from SectionWithSemester
    private fun extractSemesterFromSection(sectionWithSemester: String): Int {
        // Example: "PROG_1A" -> semester = 1
        // This assumes the semester number is the first digit after the first underscore.
        val parts = sectionWithSemester.split("_")
        if (parts.size > 1 && parts[1].isNotEmpty()) {
            return parts[1].substring(0, 1).toIntOrNull() ?: 1
        }
        return 1 // Default to 1 if parsing fails or format is unexpected
    }

    private fun setupRecyclerView(
        courses: MutableList<teacher_Courses>
    ) {
        val recyclerView = binding.courseRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Pass courses, teacherName, and teacherId to the adapter
        val adapter = Teacher_Courses_Adapter(courses, teacherName, teacherId)
        recyclerView.adapter = adapter
    }
}