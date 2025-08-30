package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.R

class GradeDistributionAdapter(
    private val gradeDistribution: Map<String, Int>
) : RecyclerView.Adapter<GradeDistributionAdapter.GradeDistributionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeDistributionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade_distribution, parent, false)
        return GradeDistributionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GradeDistributionViewHolder, position: Int) {
        holder.bind(gradeDistribution)
    }

    override fun getItemCount(): Int = 1

    class GradeDistributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gradeACell: TextView = itemView.findViewById(R.id.gradeACell)
        private val gradeBCell: TextView = itemView.findViewById(R.id.gradeBCell)
        private val gradeCCell: TextView = itemView.findViewById(R.id.gradeCCell)
        private val gradeDCell: TextView = itemView.findViewById(R.id.gradeDCell)

        fun bind(grades: Map<String, Int>) {
            gradeACell.text = grades["A"]?.toString() ?: "0"
            gradeBCell.text = grades["B"]?.toString() ?: "0"
            gradeCCell.text = grades["C"]?.toString() ?: "0"
            gradeDCell.text = grades["D"]?.toString() ?: "0"
        }
    }
}