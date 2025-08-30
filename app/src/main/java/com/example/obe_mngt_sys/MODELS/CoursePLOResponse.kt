package com.example.obe_mngt_sys.MODELS

data class CoursePLOResponse(
    val CourseCode: String,
    val CourseName: String,
    val PLOs: List<Mapping_Plos>,

)