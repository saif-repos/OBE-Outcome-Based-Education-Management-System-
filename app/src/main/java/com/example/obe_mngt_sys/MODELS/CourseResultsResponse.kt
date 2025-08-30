package com.example.obe_mngt_sys.MODELS

data class CourseResultsResponse(
    val success: Boolean,
    val courseResults: CourseResults? = null, // Make nullable
    val message: String? = null,
    val error: String? = null,
    val Dlevel:String
)
