package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CourseResultResponse(
    @SerializedName("CourseCode") val CourseCode: String,
    @SerializedName("CourseName") val CourseName: String?,
    @SerializedName("WeightageOfCourseInPlo") val WeightageOfCourseInPlo: Double,
    @SerializedName("AchievedWeightage") val AchievedWeightage: Double,
    @SerializedName("CloBreakdown") val CloBreakdown: List<CloResultResponse>?,
    @SerializedName("Dlevel")val Dlevel: String
)
