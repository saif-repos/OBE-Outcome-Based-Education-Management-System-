package com.example.obe_mngt_sys.MODELS

data class MarkUpdateRequest(
    val StudentId: String,
    val QuestionId: Int,
    val Marks:Int,
    val TaskId: Int
)
