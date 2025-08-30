package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CourseResponse(
    @SerializedName("CourseId") val courseId: String,
    @SerializedName("CourseName") val courseName: String,
)
