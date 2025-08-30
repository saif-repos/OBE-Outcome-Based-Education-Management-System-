package com.example.obe_mngt_sys.MODELS

data class TaskDetailsResponse(
    val Task: TaskDetails,
    val Questions: List<QuestionDetails>
)
