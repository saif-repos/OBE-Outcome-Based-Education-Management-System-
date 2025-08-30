package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.CloGradeResponse
import com.example.obe_mngt_sys.R

class CloGradesAdapter(
    private val cloGrades: List<CloGradeResponse>
) : RecyclerView.Adapter<CloGradesAdapter.CloGradeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloGradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clo_grade, parent, false)
        return CloGradeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CloGradeViewHolder, position: Int) {
        holder.bind(cloGrades[position])
    }

    override fun getItemCount(): Int = cloGrades.size

    class CloGradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cloDescription: TextView = itemView.findViewById(R.id.cloDescription)
        private val gradesRecyclerView: RecyclerView = itemView.findViewById(R.id.gradesRecyclerView)

        fun bind(cloGrade: CloGradeResponse) {
            cloDescription.text = "${adapterPosition + 1}. ${cloGrade.description}"

            gradesRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = GradeItemsAdapter(cloGrade.grades)
            }
        }
    }

    private class GradeItemsAdapter(
        private val gradeItems: List<CloGradeResponse.GradeItemResponse>
    ) : RecyclerView.Adapter<GradeItemsAdapter.GradeItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_grade_detail, parent, false)
            return GradeItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: GradeItemViewHolder, position: Int) {
            holder.bind(gradeItems[position])
        }

        override fun getItemCount(): Int = gradeItems.size

        class GradeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val taskType: TextView = itemView.findViewById(R.id.taskType)
            private val gradeA: TextView = itemView.findViewById(R.id.gradeA)
            private val gradeB: TextView = itemView.findViewById(R.id.gradeB)
            private val gradeC: TextView = itemView.findViewById(R.id.gradeC)
            private val gradeD: TextView = itemView.findViewById(R.id.gradeD)
            private val eemu: TextView = itemView.findViewById(R.id.eemu)
            private val avg: TextView = itemView.findViewById(R.id.avg)

            fun bind(gradeItem: CloGradeResponse.GradeItemResponse) {
                taskType.text = when {
                    gradeItem.taskType.equals("Mid", true) -> "Mid Exam"
                    gradeItem.taskType.equals("Final", true) -> "Final Exam"
                    gradeItem.smTaskId != null -> {
                        val parts = gradeItem.smTaskId.split("_")
                        if (parts.size > 1) "${gradeItem.taskType} ${parts[1]}" else gradeItem.taskType
                    }
                    else -> gradeItem.taskType
                }

                gradeA.text = gradeItem.a.toString()
                gradeB.text = gradeItem.b.toString()
                gradeC.text = gradeItem.c.toString()
                gradeD.text = gradeItem.d.toString()
                eemu.text = "(${gradeItem.a},${gradeItem.b},${gradeItem.c},${gradeItem.d})"
                avg.text = "%.2f".format(gradeItem.taskAverage)
            }
        }
    }
}