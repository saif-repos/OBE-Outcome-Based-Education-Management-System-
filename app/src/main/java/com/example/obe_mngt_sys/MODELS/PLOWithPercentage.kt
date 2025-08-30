package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PLOWithPercentage(
    @SerializedName("PLO_ID")
    val PLO_ID: Int,
    @SerializedName("MappingPercentage")
    val MappingPercentage: Float
)
