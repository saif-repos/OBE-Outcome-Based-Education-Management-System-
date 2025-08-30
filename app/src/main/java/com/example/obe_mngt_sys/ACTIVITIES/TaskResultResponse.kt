package com.example.obe_mngt_sys.ACTIVITIES

import com.example.obe_mngt_sys.MODELS.QuestionResultResponse
import com.google.gson.annotations.SerializedName

data class TaskResultResponse(
    @SerializedName("TaskId") val TaskId: Int,
    @SerializedName("TaskIdentifier") val TaskIdentifier: String?,
    @SerializedName("QuestionBreakdown") val QuestionBreakdown: List<QuestionResultResponse>?
)
