package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CrsCloMappingRequest(
    @SerializedName("oc_id") val ocId: Int,
    @SerializedName("CLO_id") val cloId: Int
)
