package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.Courses
import com.example.obe_mngt_sys.R

class CoursesAdapter(
    private var coursesList: List<Courses>,
    private val onEditClick: (Courses) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CoursesAdapter.CourseViewHolder>() {

    class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvCourseCredit: TextView = view.findViewById(R.id.tvCourseCredit)
        val tvLab: TextView = view.findViewById(R.id.tvLab)
        val tvPrerequisite: TextView = view.findViewById(R.id.tvPrerequisite)
        val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = coursesList[position]
        holder.tvCourseCode.text = course.CourseId
        holder.tvCourseName.text = course.CourseName
        holder.tvCourseCredit.text = course.CourseHours.toString()
        holder.tvLab.text = "Lab: ${course.Lab}"
        holder.tvPrerequisite.text = "Pre: ${course.Prerequisite}"

        holder.ivEdit.setOnClickListener { onEditClick(course) }
        holder.ivDelete.setOnClickListener { onDeleteClick(course.CourseId) }
    }

    override fun getItemCount(): Int = coursesList.size

    fun updateCourses(newCourses: List<Courses>) {
        coursesList = newCourses
        notifyDataSetChanged()
    }
}