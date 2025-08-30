package com.example.obe_mngt_sys.MODELS

data class ActivityResult(
    val ActivityType: String,
    val ObtainedMarks: Double,
    val TotalMarks: Double,
    val Tasks: List<TaskResult>,

)
