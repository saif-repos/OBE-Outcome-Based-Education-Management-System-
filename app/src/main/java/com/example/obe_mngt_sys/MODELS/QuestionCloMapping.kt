package com.example.obe_mngt_sys.MODELS

data class QuestionCloMapping(
    val QuestionId: Int,
    val QuestionNo: String,
    val CLOs: List<CloMapping>
)
