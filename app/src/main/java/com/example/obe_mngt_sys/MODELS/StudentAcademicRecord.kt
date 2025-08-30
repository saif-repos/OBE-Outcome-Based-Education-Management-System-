package com.example.obe_mngt_sys.MODELS

data class StudentAcademicRecord(
    val CGPA: Double,
    val TotalCreditHoursCompleted: Int,
    val Semesters: List<Semester>
)
