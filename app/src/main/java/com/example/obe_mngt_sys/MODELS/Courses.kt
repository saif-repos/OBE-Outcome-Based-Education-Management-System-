package com.example.obe_mngt_sys.MODELS



data class Courses(
    val CourseId: String,
    val CourseName: String,
    val CourseHours: Int,
    val Lab: String,
    val Prerequisite: String
)