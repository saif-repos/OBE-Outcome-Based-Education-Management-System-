package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.ActivityCommonFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommonFragment : AppCompatActivity() {
    private lateinit var binding: ActivityCommonFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommonFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Back Icon Functionality
        // Back icon par click karne par pichli activity par wapas jayein
        binding.backIcon.setOnClickListener {
            onBackPressed()
        }

        // Intent se sabhi data prapt karein
        val teacherName = intent.getStringExtra("teacherName") ?: ""
        val courseCode = intent.getStringExtra("courseCode") ?: ""
        val courseName = intent.getStringExtra("courseName") ?: ""
        val section = intent.getStringExtra("section") ?: ""
        val semester = intent.getIntExtra("semester", 0)
        val offeredCourseId = intent.getIntExtra("offeredCourseId", -1)
        val teacherId = intent.getStringExtra("teacherId") ?: "" // teacherId intent se prapt karein

        // Debugging ke liye sabhi values ko log karein
        Log.d(
            "CommonFragment", """
            Course Code: $courseCode, 
            Course Name: $courseName, 
            Section: $section, 
            Semester: $semester,
            OfferedCourseId: $offeredCourseId,
            TeacherId: $teacherId
        """.trimIndent()
        )

        // Bell icon dhoondein aur uska click listener set karein
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)

        // Bell icon ke liye click listener
        bellIcon.setOnClickListener {
            if (offeredCourseId > 0) {
                // CourseSuggestionActivity start karein aur data pass karein
                startActivity(Intent(this, CourseSuggestionActivity::class.java).apply {
                    putExtra("offeredCourseId", offeredCourseId)
                    putExtra("teacherName", teacherName)
                    putExtra("courseName", courseName)
                })
            } else {
                Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            }
        }
        setupBellIcon(offeredCourseId, teacherName, courseName)

        // TextView mein teacher ka naam set karein
        binding.textViewTeacherName.text = teacherName

        // Sabhi parameters ke saath upyukt fragment load karein
        val fragmentType = intent.getStringExtra("fragmentType")
        when (fragmentType) {
            "CLOFragment" -> loadFragment(
                CLOFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId,
                    teacherId
                )
            )

            "CLOToPLOFragment" -> loadFragment(
                CLOToPLOFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId,
                    teacherId
                )
            )

            "CLOsToActivitiesFragment" -> loadFragment(
                CLOsToActivitiesFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId
                )
            )

            "CLOsToTasksFragment" -> loadFragment(
                CLOsToTasksFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId,
                    teacherId
                )
            )

            "CreationOfTasksFragment" -> loadFragment(
                CreationOfTasksFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId,
                    teacherId
                )
            )

            "MarksDistributionFragment" -> loadFragment(
                MarksDistributionFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId
                )
            )

            "MarkSheetFragment" -> loadFragment(
                MarkSheetFragment.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId
                )
            )

            // ... inside the when block in CommonFragment.kt

            // FCR fragment ko sabhi required parameters ke saath instantiate karein
            "FCR" -> loadFragment(
                FCR.newInstance(
                    courseCode,
                    courseName,
                    section,
                    semester,
                    offeredCourseId,
                    teacherId
                )
            )
            else -> {
                Log.e("CommonFragment", "Unknown fragment type: $fragmentType")
                // Unknown fragment type ko handle karein (optional)
            }
        }
    }
// ... rest of the file

    // Fragment ko load karne ka helper function
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Optional: Back navigation ke liye back stack mein add karein
            .commit()
    }

    // Bell icon setup karne ka function (notification count ke liye)
    private fun setupBellIcon(offeredCourseId: Int, teacherName: String, courseName: String) {
        val bellIcon = findViewById<ImageView>(R.id.bellIcon)
        val countTextView = findViewById<TextView>(R.id.bellCountText)

        // Shuru mein count ko chhupayein
        countTextView.visibility = View.GONE

        // Agar offeredCourseId valid ho toh notification count prapt karein aur UI update karein
        if (offeredCourseId > 0) {
            getSuggestionCount(offeredCourseId, countTextView)
        }

        // Bell icon click listener (CourseSuggestionActivity par jaane ke liye)
        bellIcon.setOnClickListener {
            if (offeredCourseId > 0) {
                startActivity(Intent(this, CourseSuggestionActivity::class.java).apply {
                    putExtra("offeredCourseId", offeredCourseId)
                    putExtra("teacherName", teacherName)
                    putExtra("courseName", courseName)
                })
            } else {
                Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Server se suggestion count prapt karne ka function
    private fun getSuggestionCount(offeredCourseId: Int, countView: TextView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // API service ka upyog karke course suggestions count prapt karein
                val response = RetrofitInstance.apiService.GetCourseSuggestionsCount(offeredCourseId)

                Log.d("CommonFragment", "Response Code: ${response.code()}")
                Log.d("CommonFragment", "Response Body: ${response.body()}")

                if (response.isSuccessful) {
                    val count = response.body()?.count ?: 0
                    withContext(Dispatchers.Main) {
                        // UI thread par count update karein
                        if (count > 0) {
                            countView.text = count.toString()
                            countView.visibility = View.VISIBLE
                        } else {
                            countView.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CommonFragment", "Error getting count", e)
                withContext(Dispatchers.Main) {
                    countView.visibility = View.GONE
                }
            }
        }
    }
}
