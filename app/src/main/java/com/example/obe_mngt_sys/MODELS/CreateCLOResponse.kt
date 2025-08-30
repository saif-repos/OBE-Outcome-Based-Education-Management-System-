// CreateCLOResponse.kt
package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CreateCLOResponse(
    @SerializedName("Message") val message: String,
    @SerializedName("CLO_ID") val cloId: Int,
    @SerializedName("oc_id") val offeredCourseId: Int,
    @SerializedName("T_id") val teacherId: String,
    @SerializedName("description") val description: String,
    @SerializedName("C_code") val courseCode: String
)