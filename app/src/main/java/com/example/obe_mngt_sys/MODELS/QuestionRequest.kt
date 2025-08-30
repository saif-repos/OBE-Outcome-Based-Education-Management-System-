package com.example.obe_mngt_sys.MODELS

data class QuestionRequest(
    val tsk_id: Int,
    val que: String,
    val tMarks: Float,
    val dlevel:String
)
