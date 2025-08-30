package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CLOPLOMappingItem(
    @SerializedName("CLO_ID") val cloId: Int,
    @SerializedName("PLO_ID") val ploId: Int,
    @SerializedName("per") val percentage: Float,  // Changed from 'percentage' to 'per'
    @SerializedName("OriginalOcId") val originalOcId: Int,
    @SerializedName("IsFromSameCourse") val isFromSameCourse: Boolean,
    @SerializedName("CLOInfo") val cloInfo: CLOInfo? = null

)
