package com.example.obe_mngt_sys.MODELS

import com.google.gson.annotations.SerializedName

data class GradeDistributionResponse(@SerializedName("GradeDistribution") val gradeDistribution: Map<String, Int>)
