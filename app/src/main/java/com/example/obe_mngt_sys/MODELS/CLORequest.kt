package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CLORequest(
    @SerializedName("oc_id") val ocId: Int,
    @SerializedName("T_id") val teacherId: String,
    @SerializedName("description") val description: String
)