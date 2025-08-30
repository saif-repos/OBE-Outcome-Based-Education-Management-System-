package com.example.obe_mngt_sys.ACTIVITIES

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.R
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Examples : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_examples)
        val studentid = "S_001"


        val jsonTextView = findViewById<TextView>(R.id.jsonTextView)
        // Replace with your actual oc_id


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.Getstudentploresults(studentid)
                val prettyJson = GsonBuilder().setPrettyPrinting().create().toJson(response)

                withContext(Dispatchers.Main) {
                    jsonTextView.text = prettyJson
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    jsonTextView.text = "Error: ${e.message}"
                }
            }
        }




    }
}