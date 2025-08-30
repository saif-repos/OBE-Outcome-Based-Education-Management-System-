package com.example.obe_mngt_sys.MODELS

data class SelectedCoursesRequest(
    val programId: Int,
    val selectedCourseCodes: List<String>
)
