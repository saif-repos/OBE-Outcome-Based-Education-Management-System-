package com.example.obe_mngt_sys.MODELS

data class QuestionResponse(
    val Message: String,
    val Success: Boolean,
    val QuestionId: Int? = null,
    val QuestionNo: String? = null,
    val TotalMarksUsed: Float? = null,
    val RemainingMarks: Float? = null,
    val dlevel: String?= null

)
