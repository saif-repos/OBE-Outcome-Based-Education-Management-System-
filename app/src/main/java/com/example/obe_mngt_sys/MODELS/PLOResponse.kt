package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class PLOResponse(
    @SerializedName("PLO_ID") val ploId: Int,
    @SerializedName("Description") val description: String
)
