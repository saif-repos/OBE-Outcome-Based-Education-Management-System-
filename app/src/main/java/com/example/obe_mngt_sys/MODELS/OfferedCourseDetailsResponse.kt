package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class OfferedCourseDetailsResponse(
    @SerializedName("OfferedCourseId") val offeredCourseId: Int,
    @SerializedName("CourseCode") val courseCode: String,
    @SerializedName("CourseName") val courseName: String,
    @SerializedName("Section") val section: String,
    @SerializedName("Semester") val semester: Int,
    @SerializedName("Session") val session: String,
    @SerializedName("Year") val year: Int,
    @SerializedName("ProgramName") val programName: String,
    @SerializedName("CreditHours") val creditHours: Int,
    @SerializedName("TeacherName") val teacherName: String
)
