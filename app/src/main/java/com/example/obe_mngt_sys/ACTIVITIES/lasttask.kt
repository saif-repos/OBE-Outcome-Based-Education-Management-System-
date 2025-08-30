package com.example.obe_mngt_sys.ACTIVITIES

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.PloResultResponse
import com.example.obe_mngt_sys.R
import com.google.android.material.internal.ViewUtils.dpToPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class lasttask : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var scrollContainer: ScrollView
    private lateinit var contentContainer: LinearLayout // Changed from mainContainer
    private lateinit var tvHeader: TextView
    private lateinit var tvSubHeader: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lasttask)

        initViews()
        setupHeaders()
        fetchResult(
            intent.getStringExtra("UserID") ?: "",
            intent.getIntExtra("PloId", 0)
        )

    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        scrollContainer = findViewById(R.id.scrollContainer)
        contentContainer = findViewById(R.id.contentContainer) // Changed initialization
        tvHeader = findViewById(R.id.tvHeader)
        tvSubHeader = findViewById(R.id.tvSubHeader)

        // Ensure scrollContainer is properly initialized
        scrollContainer.isFocusable = true
        scrollContainer.isFocusableInTouchMode = true
    }


    private fun setupHeaders() {
        val studentName = intent.getStringExtra("UserName") ?: ""
        val ploId = intent.getIntExtra("PloId", 0)
        tvHeader.text = "PLO $ploId Results for $studentName"
        tvSubHeader.text = "Courses and their CLO contributions to PLO$ploId"
    }

    private fun fetchResult(studentId: String, ploId: Int) {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        scrollContainer.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getStudentPloResult(studentId)
                withContext(Dispatchers.Main) {
                    handlePloResponse(response, ploId)
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    showError("HTTP error: ${e.message()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error: ${e.message}")
                }
            }
        }

    }

    private fun handlePloResponse(response: Response<List<PloResultResponse>>, targetPloId: Int) {
        progressBar.visibility = View.GONE
        if (response.isSuccessful) {
            response.body()?.let { ploResults ->
                val ploData = ploResults.find { it.PloId == targetPloId }
                if (ploData == null) {
                    showError("No data available for PLO$targetPloId")
                } else {
                    displayCloResults(ploData)

                }
            } ?: showError("Empty response received")
        } else {
            showError("Server error: ${response.code()}")
        }
    }


    private fun displayCloResults(ploData: PloResultResponse) {
        runOnUiThread {
            scrollContainer.visibility = View.VISIBLE
            contentContainer.removeAllViews() // Clear all views

            // Add headers back
            contentContainer.addView(tvHeader)
            contentContainer.addView(tvSubHeader)


            val filteredCourses = ploData.CourseBreakdown
                ?.filter { course ->
                    course.CloBreakdown?.any { it.WeightInCoursePlo > 0 } == true
                }
                ?: emptyList()














        }
    }


    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        scrollContainer.visibility = View.GONE
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

}



