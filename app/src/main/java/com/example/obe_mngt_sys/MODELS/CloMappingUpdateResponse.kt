package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CloMappingUpdateResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)
