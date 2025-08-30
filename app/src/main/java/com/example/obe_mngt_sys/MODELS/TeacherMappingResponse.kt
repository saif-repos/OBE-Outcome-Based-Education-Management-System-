package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class TeacherMappingResponse(

    @SerializedName("oc_id") val offeredCourseId: Int,
    @SerializedName("C_Name") val courseName: String,
    @SerializedName("T_Name") val teacherName: String,
    @SerializedName("Status") val status: String
)
