package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class UnmappedCLO(
    @SerializedName("CLOId") val cloId: Int,
    @SerializedName("Description") val description: String,
    @SerializedName("TId") val teacherId: String
)
