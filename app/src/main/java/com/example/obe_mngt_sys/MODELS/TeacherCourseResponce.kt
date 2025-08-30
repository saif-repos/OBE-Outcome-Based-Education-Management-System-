package com.example.obe_mngt_sys.MODELS

data class TeacherCourseResponce(
    val OfferedCourseId: Int,  // Must match API property name exactly
    val CourseCode: String,
    val CourseName: String,
    val SectionWithSemester: String,
    val Session: String,     // Add this field
    val Year: Int            // Add this field
)
