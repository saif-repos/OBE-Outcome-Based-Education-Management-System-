package com.example.obe_mngt_sys.MODELS

data class OtherCourseCLO(
    val CLO_ID: Int,
    val description: String,
    val SourceCourseId: Int,
    val SourceCourseName: String,
    val CourseCode: String
)
