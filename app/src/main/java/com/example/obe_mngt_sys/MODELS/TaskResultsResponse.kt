package com.example.obe_mngt_sys.MODELS

//data class TaskResultsResponse(
//    val success: Boolean,
//    val taskName: String,
//    val totalMarks: Double,
//    val results: List<Map<String, String>>
//)

data class TaskResultsResponse(
    val success: Boolean,
    val taskName: String,
    val totalMarks: Double,
    val results: List<Map<String, Any>>,  // Changed to Map<String, Any>
    val question_ids: Map<String, Int> = emptyMap(),
    val dlevel : String
)