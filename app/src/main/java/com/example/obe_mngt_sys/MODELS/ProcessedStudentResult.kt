package com.example.obe_mngt_sys.MODELS

data class ProcessedStudentResult(
    val studentId: String,
    val studentName: String,
    val questionMarks: Map<String, QuestionData>
)
