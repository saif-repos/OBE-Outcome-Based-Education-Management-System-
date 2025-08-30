package com.example.obe_mngt_sys.MODELS

data class ErrorResponse(
    val success: Boolean,
    val message: String,
    val error: String?
)
