package com.example.obe_mngt_sys.ADAPTERS

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ACTIVITIES.CommonFragment
import com.example.obe_mngt_sys.ACTIVITIES.CourseSuggestionActivity
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.MODELS.teacher_Courses

class Teacher_Courses_Adapter(
     val courses: MutableList<teacher_Courses>,
    private val teacherName: String ,// Add teacherName parameter
    private val teacherId: String // Add teacherId parameter
) : RecyclerView.Adapter<Teacher_Courses_Adapter.CourseViewHolder>() {

    private var expandedPosition = -1

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.courseName)
        val courseSection: TextView = itemView.findViewById(R.id.courseSection)
        val expandableLayout: View = itemView.findViewById(R.id.expandableLayout)
        val cloOption: TextView = itemView.findViewById(R.id.cloOption)
        val cloToPloOption: TextView = itemView.findViewById(R.id.cloToPloOption)
        val closToTasksOption: TextView = itemView.findViewById(R.id.closToTasksOption)
        val closToActivitiesOption: TextView = itemView.findViewById(R.id.closToActivitiesOption)
        val creationOfTaskOption: TextView = itemView.findViewById(R.id.creationOfTaskOption)
        val marksDistributionOption: TextView = itemView.findViewById(R.id.MarksDistributionOption)
        //val addResultOption: TextView = itemView.findViewById(R.id.AddResultOption)
        val markSheetOption: TextView = itemView.findViewById(R.id.MarkSheetOption)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (expandedPosition != -1 && expandedPosition != position) {
                        courses[expandedPosition].isExpanded = false
                        notifyItemChanged(expandedPosition)
                    }

                    courses[position].isExpanded = !courses[position].isExpanded
                    expandedPosition = if (courses[position].isExpanded) position else -1
                    notifyItemChanged(position)
                }
            }

            // Set click listeners for all options
            cloOption.setOnClickListener { navigateToFragment("CLOFragment") }
            cloToPloOption.setOnClickListener { navigateToFragment("CLOToPLOFragment") }
            closToActivitiesOption.setOnClickListener { navigateToFragment("CLOsToActivitiesFragment") }
            closToTasksOption.setOnClickListener { navigateToFragment("CLOsToTasksFragment") }
            creationOfTaskOption.setOnClickListener { navigateToFragment("CreationOfTasksFragment") }
            marksDistributionOption.setOnClickListener { navigateToFragment("MarksDistributionFragment") }
            markSheetOption.setOnClickListener { navigateToFragment("MarkSheetFragment") }
            // Add this line for FCR
            itemView.findViewById<TextView>(R.id.FCR).setOnClickListener { navigateToFragment("FCR") }
        }

        private fun navigateToFragment(fragmentType: String) {
            val course = courses[adapterPosition]
            val intent = Intent(itemView.context, CommonFragment::class.java).apply {
                putExtra("fragmentType", fragmentType)
                putExtra("courseCode", course.courseCode)
                putExtra("courseName", course.courseName)
                putExtra("section", course.section)
                putExtra("semester", course.semester)
                putExtra("offeredCourseId", course.offeredCourseId) // Add offeredCourseId
                putExtra("teacherName", teacherName) // Use the passed teacherName
                putExtra("teacherId", teacherId) // Pass teacherId to CommonFragment
            }
            itemView.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_techer_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.courseName.text = course.courseName
        holder.courseSection.text = course.section

        holder.expandableLayout.visibility = if (course.isExpanded) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = courses.size
}