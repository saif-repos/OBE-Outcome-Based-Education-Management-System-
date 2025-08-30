package com.example.obe_mngt_sys.MODELS

data class ApprovalResponse(
    val success: Boolean,
    val message: String?,
    val oc_id: Int?,
    val T_ID: String?,
    val statuss: String?
)
