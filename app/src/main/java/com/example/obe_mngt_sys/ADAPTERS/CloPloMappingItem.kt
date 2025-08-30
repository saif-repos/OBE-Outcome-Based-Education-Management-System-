package com.example.obe_mngt_sys.ADAPTERS

// CloPloMappingItem.kt
data class CloPloMappingItem(
    val cloId: Int,
    val cloDescription: String,
    val ploMappings: Map<Int, Boolean> // PLO ID to whether it's mapped
)