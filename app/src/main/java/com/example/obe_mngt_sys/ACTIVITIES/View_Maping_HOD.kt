package com.example.obe_mngt_sys.ACTIVITIES

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.PLOCloMappingResponse
import com.example.obe_mngt_sys.MODELS.SuggestionRequest
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class View_Maping_HOD : AppCompatActivity() {

    private lateinit var tableHeader: TableLayout
    private lateinit var tableLayout: TableLayout
    private lateinit var buttonApproved: Button
    private lateinit var buttonSend: Button
    private lateinit var editTextSuggestion: EditText
    private var offeredCourseId: Int = -1
    private var currentStatus: String = "pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_maping_hod)

        // Initialize views
        tableHeader = findViewById(R.id.tableHeader)
        tableLayout = findViewById(R.id.tableLayout)
        buttonApproved = findViewById(R.id.buttonApproved)
        buttonSend = findViewById(R.id.buttonSend)
        editTextSuggestion = findViewById(R.id.editTextSuggestion)

        // Get passed data
        offeredCourseId = intent.getIntExtra("offeredCourseId", -1)
        val courseName = intent.getStringExtra("courseName") ?: "N/A"
        val hodName = intent.getStringExtra("hodName") ?: ""
        currentStatus = intent.getStringExtra("status") ?: "pending"

        // Set up UI
        findViewById<TextView>(R.id.textViewHODName).text = hodName
        findViewById<TextView>(R.id.textViewCourseName).text = courseName

        // Set button state based on current status
        updateButtonState()

        // Set up back button
        findViewById<Button>(R.id.backIcon).setOnClickListener {
            onBackPressed()
        }

        // Set up approval button
        buttonApproved.setOnClickListener {
            approveMapping()
        }

        // Set up send suggestion button
        buttonSend.setOnClickListener {
            sendSuggestion()
        }

        // Load mapping data
        if (offeredCourseId != -1) {
            fetchCLOPLOMapping(offeredCourseId)
        } else {
            showError("Invalid course ID")
        }
    }

    private fun updateButtonState() {
        when (currentStatus.lowercase()) {
            "approved" -> {
                buttonApproved.apply {
                    text = "APPROVED"
                    setBackgroundColor(Color.GREEN)
                    isEnabled = false
                }
                buttonSend.apply {
                    isEnabled = true
                    text = "Suggest"
                    setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.Aqua))
                }
            }
            "suggested" -> {
                buttonApproved.apply {
                    text = "Approve"
                    setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.Aqua))
                    isEnabled = true
                }
                buttonSend.apply {
                    isEnabled = true
                    text = "Update Suggestion"
                    setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.Aqua))
                }
            }
            else -> { // pending or other status
                buttonApproved.apply {
                    text = "Approve"
                    setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.Aqua))
                    isEnabled = true
                }
                buttonSend.apply {
                    isEnabled = true
                    text = "Send"
                    setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.Aqua))
                }
            }
        }
    }
    private fun approveMapping() {
        if (offeredCourseId == -1) {
            showError("Invalid course ID")
            return
        }

        buttonApproved.isEnabled = false
        buttonApproved.text = "Processing..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.approveMapping(offeredCourseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result?.success == true && result.statuss == "approved") {
                            currentStatus = "approved"
                            updateButtonState()
                            showError("Mapping approved successfully!")
                        } else {
                            buttonApproved.isEnabled = true
                            buttonApproved.text = "Approve"
                            showError(result?.message ?: "Approval failed")
                        }
                    } else {
                        buttonApproved.isEnabled = true
                        buttonApproved.text = "Approve"
                        showError("Failed to approve: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    buttonApproved.isEnabled = true
                    buttonApproved.text = "Approve"
                    showError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun sendSuggestion() {
        val suggestion = editTextSuggestion.text.toString().trim()
        if (suggestion.isEmpty()) {
            showError("Please enter your suggestion")
            return
        }

        buttonSend.isEnabled = false
        buttonSend.text = "Sending..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.sendSuggestion(
                    SuggestionRequest(offeredCourseId, suggestion)
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.success) {
                                editTextSuggestion.text.clear()
                                currentStatus = "suggested" // Force status to suggested
                                updateButtonState()
                                showError(it.message ?: "Suggestion sent successfully")
                            } else {
                                showError(it.message ?: "Failed to send suggestion")
                            }
                        }
                    } else {
                        showError("Failed to send suggestion: ${response.errorBody()?.string()}")
                    }
                    buttonSend.isEnabled = true
                    buttonSend.text = if (currentStatus == "suggested") "Update Suggestion" else "Send"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    buttonSend.isEnabled = true
                    buttonSend.text = if (currentStatus == "suggested") "Update Suggestion" else "Send"
                    showError("Network error: ${e.message}")
                }
            }
        }
    }
    private fun fetchCLOPLOMapping(offeredCourseId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getPloCloMappingForHodView(offeredCourseId)
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        if (data.isNotEmpty()) {
                            populateGrid(data)
                        } else {
                            showEmptyState()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Failed to load mappings")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Network error: ${e.message}")
                }
            }
        }
    }

    private fun populateGrid(data: List<PLOCloMappingResponse>) {
        tableHeader.removeAllViews()
        tableLayout.removeAllViews()

        val groupedByClo = data.groupBy { it.cloId }
        val allPloIds = data.map { it.ploId }.distinct().sorted()

        // Header Row
        val headerRow = TableRow(this).apply {
            addView(createHeaderTextView("CLO"))
            allPloIds.forEach { ploId ->
                addView(createHeaderTextView("PLO$ploId"))
            }
        }
        tableHeader.addView(headerRow)

        // Data Rows
        groupedByClo.forEach { (cloId, mappings) ->
            val row = TableRow(this).apply {
                addView(createCellTextView("CLO$cloId"))
                val ploMap = mappings.associateBy({ it.ploId }, { it.percentage })
                allPloIds.forEach { ploId ->
                    val percentage = ploMap[ploId] ?: 0f
                    addView(createCellTextView(if (percentage > 0f) "%.1f%%".format(percentage) else "-"))
                }
            }
            tableLayout.addView(row)
        }

        // Total Row
        val ploSumMap = mutableMapOf<Int, Float>()
        data.forEach { item ->
            ploSumMap[item.ploId] = (ploSumMap[item.ploId] ?: 0f) + item.percentage
        }

        val totalRow = TableRow(this).apply {
            addView(createBoldTextView("TOTAL"))
            allPloIds.forEach { ploId ->
                val sum = ploSumMap[ploId] ?: 0f
                val cell = createCellTextView("%.1f%%".format(sum))
                cell.background = if (sum >= 50f)
                    ContextCompat.getDrawable(this@View_Maping_HOD, R.drawable.green_rounded_box)
                else
                    ContextCompat.getDrawable(this@View_Maping_HOD, R.drawable.red_rounded_box)
                addView(cell)
            }
        }
        tableLayout.addView(totalRow)
    }

    private fun createHeaderTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.light_gray))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun createCellTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }
    }

    private fun createBoldTextView(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setBackgroundColor(ContextCompat.getColor(this@View_Maping_HOD, R.color.light_gray))
        }
    }

    private fun showEmptyState() {
        val row = TableRow(this).apply {
            addView(TextView(this@View_Maping_HOD).apply {
                text = "No mapping data available"
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
            })
        }
        tableLayout.addView(row)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun startActivity(
            context: AppCompatActivity,
            offeredCourseId: Int,
            courseName: String,
            hodName: String
        ) {
            val intent = Intent(context, View_Maping_HOD::class.java).apply {
                putExtra("offeredCourseId", offeredCourseId)
                putExtra("courseName", courseName)
                putExtra("hodName", hodName)
            }
            context.startActivity(intent)
        }
    }
}
