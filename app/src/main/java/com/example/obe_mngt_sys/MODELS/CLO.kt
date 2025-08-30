package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CLO(
    val CLO_ID: Int,
    @SerializedName("Display_CLO_ID") val displayCloId: String,
    val description: String,
    val oc_id: Int,
    val T_id: String
) // Description of the CLO
