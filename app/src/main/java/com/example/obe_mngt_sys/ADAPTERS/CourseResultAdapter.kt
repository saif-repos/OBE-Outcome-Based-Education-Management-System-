package com.example.obe_mngt_sys.ADAPTERS



import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.CourseResult
import com.example.obe_mngt_sys.R


class CourseResultAdapter(private val courseResults: List<CourseResult>) :
    RecyclerView.Adapter<CourseResultAdapter.CourseResultViewHolder>() {

    // ViewHolder کلاس جو ہر آئٹم کے ویوز کو ہولڈ کرتی ہے
    inner class CourseResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.textViewCourseName)
        val grade: TextView = itemView.findViewById(R.id.textViewGrade)
        val marks: TextView = itemView.findViewById(R.id.textViewMarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseResultViewHolder {
        // لیآؤٹ انفلیٹ کر رہے ہیں
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_result, parent, false)
        return CourseResultViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CourseResultViewHolder, position: Int) {
        val currentItem = courseResults[position]

        // ڈیٹا کو ویوز میں سیٹ کر رہے ہیں
        holder.courseName.text = currentItem.CourseName
        holder.grade.text = currentItem.Grade
        holder.marks.text = currentItem.TotalMarks.toString()

        // گریڈ کے مطابق رنگ تبدیل کر رہے ہیں
        when (currentItem.Grade) {
            "A", "A+" -> holder.grade.setTextColor(Color.parseColor("#4CAF50")) // Green
            "B", "B+" -> holder.grade.setTextColor(Color.parseColor("#2196F3")) // Blue
            "C", "C+" -> holder.grade.setTextColor(Color.parseColor("#FF9800")) // Orange
            "D", "F" -> holder.grade.setTextColor(Color.parseColor("#F44336")) // Red
            else -> holder.grade.setTextColor(Color.BLACK)
        }
    }
    override fun getItemCount() = courseResults.size
}