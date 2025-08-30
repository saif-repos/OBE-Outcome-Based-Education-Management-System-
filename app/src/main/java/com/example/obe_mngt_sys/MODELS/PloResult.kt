package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PloResult(
    @SerializedName("PloId") val PloId: Int,
    @SerializedName("PloDescription") val PloDescription: String,
    @SerializedName("CourseBreakdown") val CourseBreakdown: List<CoursePLOResponse>?,
    @SerializedName("FinalPloScore") val FinalPloScore: Double
)
