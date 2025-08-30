package com.example.obe_mngt_sys.MODELS

data class TeacherDashboardResponse(
    val TeacherName: String,
    val Courses: List<TeacherCourseResponce>
)
