package com.example.obe_mngt_sys.MODELS

data class ApiResponse(
    val Message: String,
    val Task: TaskDetails?,
    val Questions: List<QuestionDetails>?,
    val QuestionsTotalMarks: Float?,
    val RemainingMarks: Float?
)
