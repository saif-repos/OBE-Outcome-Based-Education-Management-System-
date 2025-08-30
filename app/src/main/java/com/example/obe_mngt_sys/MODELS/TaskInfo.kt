package com.example.obe_mngt_sys.MODELS

data class TaskInfo(
    val taskId: Int,
    val taskName: String,
    val totalMarks: Double,
    val questionCount: Int,
    val questions: List<QuestionInfoo>
)
