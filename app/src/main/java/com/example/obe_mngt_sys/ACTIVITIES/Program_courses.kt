package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.obe_mngt_sys.*
import com.example.obe_mngt_sys.databinding.ActivityProgramCoursesBinding
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import kotlinx.coroutines.launch

class Program_courses : AppCompatActivity() {
    private lateinit var binding: ActivityProgramCoursesBinding
    private var programId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProgramCoursesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get Passed Data
        val hodId = intent.getStringExtra("HOD_ID")
        val hodName = intent.getStringExtra("HOD_NAME")
        programId = intent.getIntExtra("PROGRAM_ID", -1)
        val programName = intent.getStringExtra("PROGRAM_NAME")

        // Display HOD & Program Name
        binding.textViewHODName.text = hodName
        binding.programName.text = programName

        // Initialize notification badge
        setupNotificationBadge()

        // Load Default Fragment (Courses) if programId is valid
        if (programId != -1) {
            loadFragment(FragmentCourses.newInstance(programId))
            updateButtonStyles(binding.btnCourses)
            fetchNotificationCount() // Fetch initial notification count
        } else {
            Log.e("ProgramCourses", "Invalid programId received.")
        }

        // Back Button Functionality
        binding.backIcon.setOnClickListener {
            val intent = Intent(this, HOD_DASHBOARD::class.java).apply {
                putExtra("UserID", hodId)
                putExtra("UserName", hodName)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }

        // Button Click Listeners for Fragment Switching
        binding.btnCourses.setOnClickListener {
            if (programId != -1) {
                loadFragment(FragmentCourses.newInstance(programId))
                updateButtonStyles(binding.btnCourses)
            }
        }

        binding.btnPLOS.setOnClickListener {
            if (programId != -1) {
                loadFragment(FragmentPLOs.newInstance(programId))
                updateButtonStyles(binding.btnPLOS)
            }
        }

        binding.btnMapping.setOnClickListener {
            if (programId != -1) {
                loadFragment(fragment_Mapping.newInstance(programId))
                updateButtonStyles(binding.btnMapping)
            }
        }

        binding.btnCourseAllocation.setOnClickListener {
            if (programId != -1) {
                loadFragment(FragmentCourseAllocation.newInstance(programId))
                updateButtonStyles(binding.btnCourseAllocation)
            }
        }
    }

    private fun setupNotificationBadge() {
        binding.addIcon.setOnClickListener {
            if (programId != -1) {
                val intent = Intent(this, MAPPING_FOR_APPROVALS_SCREEN::class.java).apply {
                    putExtra("PROGRAM_ID", programId)
                    putExtra("HOD_NAME", intent.getStringExtra("HOD_NAME"))
                }
                startActivity(intent)
            }
        }
    }

    private fun fetchNotificationCount() {
        if (programId == -1) return

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.getTeacherMappingsCountByProgram(programId)
                if (response.isSuccessful) {
                    val count = response.body()?.count ?: 0
                    updateNotificationBadge(count)
                }
            } catch (e: Exception) {
                Log.e("ProgramCourses", "Error fetching notification count", e)
            }
        }
    }

    private fun updateNotificationBadge(count: Int) {
        runOnUiThread {
            if (count > 0) {
                binding.notificationBell.text = if (count > 99) "99+" else count.toString()
                binding.notificationBell.visibility = View.VISIBLE
                startBadgeAnimation()
            } else {
                binding.notificationBell.visibility = View.GONE
                stopBadgeAnimation()
            }
        }
    }

    private fun startBadgeAnimation() {
        val anim = AlphaAnimation(0.2f, 1.0f).apply {
            duration = 1000
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.notificationBell.startAnimation(anim)
    }

    private fun stopBadgeAnimation() {
        binding.notificationBell.clearAnimation()
    }

    override fun onResume() {
        super.onResume()
        fetchNotificationCount() // Refresh count when returning to activity
    }

    private fun loadFragment(fragment: Fragment) {
        Log.d("FragmentDebug", "Loading fragment: ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitAllowingStateLoss()
    }

    private fun updateButtonStyles(selectedButton: android.widget.Button) {
        val buttons = listOf(
            binding.btnCourses, binding.btnPLOS,
            binding.btnMapping, binding.btnCourseAllocation
        )

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.Aqua))
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray))
                button.setTextColor(Color.parseColor("#1A237E"))
            }
        }
    }
}