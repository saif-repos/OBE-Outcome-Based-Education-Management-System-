package com.example.obe_mngt_sys.MODELS

data class TaskDetails(
    val tsk_id: Int,
    val taskid: String,
    val tMarks: Float,
    val ActivityType: String?,
    val FurtherActivityType: String?
)
