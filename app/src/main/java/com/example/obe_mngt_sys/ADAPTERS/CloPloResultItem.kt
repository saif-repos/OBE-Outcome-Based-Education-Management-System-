package com.example.obe_mngt_sys.ADAPTERS

// CloPloResultItem.kt
data class CloPloResultItem(
    val cloId: Int,
    val cloDescription: String,
    val ploValues: Map<Int, Double?> // PLO ID to value (null if not mapped)
)