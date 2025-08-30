package com.example.obe_mngt_sys.MODELS

//data class TaskResponse(
//    val Message: String,
//    val TaskId: Int,
//    val TaskIdentifier: String,
//    val SmallTaskId: Int?
//)
// TaskResponse.kt
data class TaskResponse(
    val Message: String,
    val TaskIds: List<Int>? = null,
    val TaskIdentifiers: List<String>? = null,
    val SmallTaskId: Int? = null
)
