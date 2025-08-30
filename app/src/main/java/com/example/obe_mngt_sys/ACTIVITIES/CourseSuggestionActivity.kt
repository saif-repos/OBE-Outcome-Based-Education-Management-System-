package com.example.obe_mngt_sys.ACTIVITIES

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.SuggestionAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.Suggestion
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CourseSuggestionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_suggestion)

        // Get data from intent
        val teacherName = intent.getStringExtra("teacherName") ?: ""
        val courseName = intent.getStringExtra("courseName") ?: ""
        val offeredCourseId = intent.getIntExtra("offeredCourseId", 0)

        // Set up UI
        findViewById<TextView>(R.id.textViewTeacherName).text = teacherName
        findViewById<TextView>(R.id.textViewCourseName).text = courseName
        findViewById<ImageView>(R.id.backIcon).setOnClickListener { onBackPressed() }

        // Setup RecyclerView
        recyclerView = findViewById(R.id.suggestionsRecyclerView)
        adapter = SuggestionAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        if (offeredCourseId > 0) {
            loadSuggestions(offeredCourseId)
        } else {
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadSuggestions(offeredCourseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getCourseSuggestions(offeredCourseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val suggestions = response.body()?.suggestions ?: emptyList()
                        adapter.updateSuggestions(suggestions)
                    } else {
                        Toast.makeText(this@CourseSuggestionActivity,
                            "Failed to load suggestions", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CourseSuggestionActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}