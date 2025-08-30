package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CloActivityMapping(
    @SerializedName("CLO_ID") val cloId: Int,
    @SerializedName("typeId") val typeId: Int,
    @SerializedName("ActivityName") val activityName: String, // Added activity name
    @SerializedName("per") val percentage: Int,
    @SerializedName("OC_ID") val ocId: Int
)