package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class TaskResponsee(
    @SerializedName("tsk_id") val tsk_id: Int,
    @SerializedName("tMarks") val tMarks: Float,
    // Add other task properties as needed
)
