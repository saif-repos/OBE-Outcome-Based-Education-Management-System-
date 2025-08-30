package com.example.obe_mngt_sys.MODELS

data class CourseTask(
    val Tsk_ID: Int,          // Primary key from task table (1, 2, 3...)
    val Typee: String,        // Activity type ("HW", "Lab", "Mid")
    val Task_ID: String,      // Task identifier ("HW_1", "Lab_1", "Mid")
    val SmallTask_ID: String? = null,  // Nullable for non-HW tasks
    val Type_ID: Int,         // Activity type ID (1, 2, 4, 7...)
    val tMarks: Float? = null // Optional total marks
)