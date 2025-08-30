package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CloResultResponse(
    @SerializedName("CloId") val CloId: Int,
    @SerializedName("CloDescription") val CloDescription: String?,
    @SerializedName("WeightInCoursePlo") val WeightInCoursePlo: Double,
    @SerializedName("AchievedWeightedScore") val AchievedWeightedScore: Double,
    @SerializedName("ActivityBreakdown") val ActivityBreakdown: List<ActivityResultResponse>?,
    val Dlevel:String
)


