package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class QuestionMarks(
    val marks: String,
    @SerializedName("question_id") val questionId: Int
)
