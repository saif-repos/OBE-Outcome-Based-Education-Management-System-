package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CountResponse(
    @SerializedName("Count")
    val count: Int
)
