package com.example.obe_mngt_sys.MODELS

data class Semester(
    val sesionyear: String,
    val Semester: Int,
    val Courses: List<CourseResult>,
    val TotalCreditHours: Int,
    val TotalQP: Double,
    val GPA: Double
)
