package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class StatusUpdateResponse(

    @SerializedName("Message") val message: String,
    @SerializedName("oc_id") val ocId: Int,
    @SerializedName("T_ID") val teacherId: String,
    @SerializedName("statuss") val status: String

)
