package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class MappingResponse(
    @SerializedName("Message") val message: String,
    @SerializedName("oc_id") val ocId: Int,
    @SerializedName("CLO_id") val cloId: Int
)
