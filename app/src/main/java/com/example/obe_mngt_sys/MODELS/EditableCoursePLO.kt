package com.example.obe_mngt_sys.MODELS

data class EditableCoursePLO(
    val courseCode: String, // Course code
    val ploId: Int,         // PLO ID
    var percentage: Int     // Percentage (editable)
)
