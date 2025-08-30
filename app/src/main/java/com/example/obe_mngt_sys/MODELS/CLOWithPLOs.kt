package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CLOWithPLOs(
    @SerializedName("CLO_ID")
    val CLO_ID: Int,
    @SerializedName("CLO_Description")
    val CLO_Description: String,
    @SerializedName("PLOs")
    val PLOs: List<PLOWithPercentage>
)

