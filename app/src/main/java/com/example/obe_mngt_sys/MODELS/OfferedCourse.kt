package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class OfferedCourse(

    @SerializedName("C_code") val C_code: String,
    @SerializedName("cname") val cname: String,
    @SerializedName("Tname") val Tname: String?,
    @SerializedName("Section") val Section: String,
    @SerializedName("Semester") val Semester: Int,
    @SerializedName("AssignedTeacher") val AssignedTeacher: String?,
    @SerializedName("oc_id") val Oc_id: Int,  // Ensure proper serialization
    @SerializedName("T_id") val T_ID: String,  // Ensure proper serialization
)