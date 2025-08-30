package com.example.obe_mngt_sys.MODELS

data class TaskDeleteResponse(
    val success: Boolean,
    val message: String,
    val deletedTaskId: Int,
    val deletedQuestionsCount: Int,
    val deletedSmallTasksCount: Int
)
