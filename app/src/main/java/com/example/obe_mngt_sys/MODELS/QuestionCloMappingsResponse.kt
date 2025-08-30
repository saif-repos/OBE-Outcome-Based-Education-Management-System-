package com.example.obe_mngt_sys.MODELS

import com.example.obe_mngt_sys.ACTIVITIES.CLOsToTasksFragment.TaskMapping

data class QuestionCloMappingsResponse(
    val success: Boolean,
    val data: List<TaskMapping>)
