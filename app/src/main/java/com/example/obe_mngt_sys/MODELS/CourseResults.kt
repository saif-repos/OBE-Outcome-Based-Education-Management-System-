package com.example.obe_mngt_sys.MODELS

data class CourseResults(
    val courseId: Int,
    val tasks: List<TaskInfo>? = null,
    val studentResults: List<StudentResultMarkSheet>? = null
)
