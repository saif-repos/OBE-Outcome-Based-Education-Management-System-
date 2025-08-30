package com.example.obe_mngt_sys.MODELS

data class StudentResultMarkSheet(
    val s_id: String,
    val student_name: String?,
    val results: Map<String, QuestionResult>? = null
)
