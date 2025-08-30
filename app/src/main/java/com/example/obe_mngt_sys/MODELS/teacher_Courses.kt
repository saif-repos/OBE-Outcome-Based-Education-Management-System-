package com.example.obe_mngt_sys.MODELS

data class teacher_Courses(
    val offeredCourseId: Int,  // This should match what you use in the adapter
    val courseCode: String,
    val courseName: String,
    val section: String,
    val semester: Int,
    var isExpanded: Boolean = false
)
