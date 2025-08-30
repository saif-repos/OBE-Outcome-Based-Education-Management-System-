package com.example.obe_mngt_sys.MODELS

data class TaskRequest(
    val typeId: Int,
    val oc_id: Int,
    val tMarks: Float,
    val further_id: Int = 0
)
