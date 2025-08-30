package com.example.obe_mngt_sys.MODELS

data class CourseResult(


    val CourseCode: String,
    val CourseName: String,
    val Section: String,
    val CreditHours: Int,
    val Activities: List<ActivityResult>,
    val TotalMarks: Double,
    val TotalPossibleMarks: Double,
    val Grade: String,
    val QP: Double

)
