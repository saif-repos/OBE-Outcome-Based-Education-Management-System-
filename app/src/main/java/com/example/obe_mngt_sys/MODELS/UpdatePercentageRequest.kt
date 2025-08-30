package com.example.obe_mngt_sys.MODELS

data class UpdatePercentageRequest(
    val questionId: Int,
    val cloId: Int,
    val newPercentage: String
)
