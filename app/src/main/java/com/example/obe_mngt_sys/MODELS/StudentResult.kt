package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class StudentResult(
    @SerializedName("s_id") val studentId: String,
    @SerializedName("student_name") val studentName: String,
    // Dynamic question fields will be handled by Gson
    val questions: Map<String, QuestionMarks> = emptyMap()
)
