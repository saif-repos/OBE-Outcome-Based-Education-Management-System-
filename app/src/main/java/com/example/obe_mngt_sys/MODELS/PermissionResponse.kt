package com.example.obe_mngt_sys.MODELS

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PermissionResponse(
    @SerializedName("Result")
    val Result: Boolean
)