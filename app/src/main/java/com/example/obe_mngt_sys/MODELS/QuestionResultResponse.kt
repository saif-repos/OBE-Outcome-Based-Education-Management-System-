package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class QuestionResultResponse(
    @SerializedName("QuestionId") val QuestionId: Int,
    @SerializedName("QuestionText") val QuestionText: String?,
    @SerializedName("TotalMarks") val TotalMarks: Double?,
    @SerializedName("ObtainedMarks") val ObtainedMarks: Double,
    @SerializedName("QuestionPerformance") val QuestionPerformance: Double,
    @SerializedName("WeightInActivityClo") val WeightInActivityClo: Double,
    @SerializedName("AchievedWeightedMarks") val AchievedWeightedMarks: Double,
    @SerializedName("PossibleWeightedMarks") val PossibleWeightedMarks: Double
)
