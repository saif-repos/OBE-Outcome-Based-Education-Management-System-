package com.example.obe_mngt_sys.MODELS

data class TeacherCoursesBySessionResponse(
    val TeacherName: String,
    val Courses: List<TeacherCourseResponce>, // This will use the updated TeacherCourseResponce
    val AvailableSessions: List<SessionYearOption>
) // The spinner options
