package com.example.obe_mngt_sys.MODELS

data class QuestionUpdate(
    val tq_id: Int, // 0 for new questions
    val que: String,
    val tMarks: Float
)
