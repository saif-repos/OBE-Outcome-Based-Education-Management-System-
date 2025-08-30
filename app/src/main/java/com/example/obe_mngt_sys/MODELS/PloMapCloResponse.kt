package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PloMapCloResponse(
    @SerializedName("CLO_ID")
    val cloId: Int,

    @SerializedName("PLO_ID")
    val ploId: Int,

    @SerializedName("per")
    val per: Int,  // Changed from Int to Double since your response shows 20.0

    @SerializedName("CLOInfo")
    val cloInfo: CLOInfo,

    @SerializedName("OriginalOcId")
    val originalOcId: Int,

    @SerializedName("IsFromSameCourse")
    val isFromSameCourse: Boolean
)

data class CLOInfoo(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("Description")
    val description: String,

    @SerializedName("T_id")
    val tId: String
)
