package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CLOInfo(@SerializedName("Id") val id: Int,
                   @SerializedName("Description") val description: String,
                   @SerializedName("T_id") val teacherId: String)
