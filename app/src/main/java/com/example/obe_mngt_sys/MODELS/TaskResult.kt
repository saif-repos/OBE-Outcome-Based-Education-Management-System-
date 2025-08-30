package com.example.obe_mngt_sys.MODELS

data class TaskResult(
    val TaskId: String?,
    val ObtainedMarks: Double,
    val TotalMarks: Double,
    val Questions: List<QuestionResults>
)
