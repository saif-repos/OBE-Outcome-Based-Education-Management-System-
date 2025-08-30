package com.example.obe_mngt_sys.MODELS

import com.example.obe_mngt_sys.ACTIVITIES.TaskResultResponse
import com.google.gson.annotations.SerializedName

data class ActivityResultResponse(
    @SerializedName("ActivityTypeId") val ActivityTypeId: Int,
    @SerializedName("ActivityTypeName") val ActivityTypeName: String?,
    @SerializedName("WeightInOverallClo") val WeightInOverallClo: Double,
    @SerializedName("AchievedWeightedScore") val AchievedWeightedScore: Double,
    @SerializedName("TaskBreakdown") val TaskBreakdown: List<TaskResultResponse>?,
    val Dlevel:String
    )
