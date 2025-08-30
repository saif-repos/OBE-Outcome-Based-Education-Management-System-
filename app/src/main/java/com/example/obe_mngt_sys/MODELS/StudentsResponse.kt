package com.example.obe_mngt_sys.MODELS

data class StudentsResponse(
    val success: Boolean,
    val message: String?,
    val programId: Int,
    val semester: String,
    val section: String,
    val count: Int,
    val students: List<StudentInfo>
)
