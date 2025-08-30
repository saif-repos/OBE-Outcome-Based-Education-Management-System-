package com.example.obe_mngt_sys.MODELS

data class SuggestionResponse(
    val success: Boolean,
    val message: String,
    val suggestions: List<Suggestion>
)
