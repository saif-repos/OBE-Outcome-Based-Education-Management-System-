package com.example.obe_mngt_sys.MODELS

data class QuestionUpdateRequest(
    val tsk_id: Int,
    val Questions: List<QuestionUpdate>,
    val DeletedQuestionIds: List<Int> = emptyList()
)
