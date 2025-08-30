package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PloResultResponse(
    @SerializedName("PloId") val PloId: Int,
    @SerializedName("PloDescription") val PloDescription: String,
    @SerializedName("CourseBreakdown") val CourseBreakdown: List<CourseResultResponse>?,
    @SerializedName("FinalPloScore") val FinalPloScore: Double,
    @SerializedName("Dlevel") val Dlevel:String
)
