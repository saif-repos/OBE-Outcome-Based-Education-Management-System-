package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.CourseResponse
import com.example.obe_mngt_sys.R

class CoursesSelectionAdapter(
    private var coursesList: List<CourseResponse>,
    private val onCourseSelected: (String, Boolean) -> Unit
) : RecyclerView.Adapter<CoursesSelectionAdapter.ViewHolder>(), Filterable {

    private var filteredCoursesList: List<CourseResponse> = coursesList

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbCourse: CheckBox = itemView.findViewById(R.id.cbCourse)

        fun bind(course: CourseResponse) {
            cbCourse.text = "${course.courseId} - ${course.courseName}"
            cbCourse.setOnCheckedChangeListener { _, isChecked ->
                onCourseSelected(course.courseId, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredCoursesList[position])
    }

    override fun getItemCount(): Int {
        return filteredCoursesList.size
    }

    // Implement Filterable interface
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                filteredCoursesList = if (query.isEmpty()) {
                    coursesList // Return the full list if the query is empty
                } else {
                    coursesList.filter { course ->
                        course.courseId.lowercase().contains(query) || // Filter by course ID
                                course.courseName.lowercase().contains(query) // Filter by course name
                    }
                }
                val results = FilterResults()
                results.values = filteredCoursesList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredCoursesList = results?.values as List<CourseResponse>
                notifyDataSetChanged() // Notify the adapter that the data has changed
            }
        }
    }

    // Function to update the full list of courses
    fun updateCoursesList(newCoursesList: List<CourseResponse>) {
        coursesList = newCoursesList
        filteredCoursesList = newCoursesList
        notifyDataSetChanged()
    }
}