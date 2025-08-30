package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class CloGradeResponse( @SerializedName("clo_id") val cloId: Int,
                             @SerializedName("description") val description: String,
                             @SerializedName("clo_average") val cloAverage: Double,
                             @SerializedName("grades") val grades: List<GradeItemResponse>
) {
    data class GradeItemResponse(
        @SerializedName("task_type") val taskType: String,
        @SerializedName("task_id") val taskId: String?,
        @SerializedName("smtask_id") val smTaskId: String?,
        @SerializedName("A") val a: Int,
        @SerializedName("B") val b: Int,
        @SerializedName("C") val c: Int,
        @SerializedName("D") val d: Int,
        @SerializedName("task_average") val taskAverage: Double
    )
}
