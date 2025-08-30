package com.example.obe_mngt_sys.MODELS

data class MarksDistributionResponse(
    val oc_id: Int,
    val hw: Int,
    val mid: Int,
    val final: Int,
    val lab: Int,
    val gtotal: Int,
    val qp: Int
)
