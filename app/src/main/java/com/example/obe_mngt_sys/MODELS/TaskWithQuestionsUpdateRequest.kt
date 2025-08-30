package com.example.obe_mngt_sys.MODELS

data class TaskWithQuestionsUpdateRequest(
    val TaskTotalMarks: Float? = null,
    val Questions: List<QuestionUpdate>,
    val DeletedQuestionIds: List<Int> = emptyList()
)
