package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PLOCloMappingResponse(
    @SerializedName("CLO_ID") val cloId: Int,
    @SerializedName("PLO_ID") val ploId: Int,
    @SerializedName("per") val percentage: Float,
    @SerializedName("OriginalOcId") val originalOcId: Int
)
