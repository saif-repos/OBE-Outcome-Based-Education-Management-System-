package com.example.obe_mngt_sys.ADAPTERS

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.OfferedCourse
import com.example.obe_mngt_sys.R

class CourseAllocationAdapter(
    var courses: List<OfferedCourse>
) : RecyclerView.Adapter<CourseAllocationAdapter.ViewHolder>() {

    private val groupedCourses: Map<String, List<OfferedCourse>>
        get() = courses.groupBy { it.cname }

    private val selectedMappings = mutableListOf<Mapping>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.courseName)
        val teacherContainer: LinearLayout = itemView.findViewById(R.id.teacherContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_allocation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val courseName = groupedCourses.keys.elementAt(position)
        val coursesForName = groupedCourses[courseName] ?: emptyList()

        holder.courseName.text = courseName
        holder.teacherContainer.removeAllViews()

        coursesForName.forEach { course ->
            val teacherView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_teacher, holder.teacherContainer, false)

            val teacherName: TextView = teacherView.findViewById(R.id.teacherName)
            val semesterSection: TextView = teacherView.findViewById(R.id.semesterSection)
            val radioButton: RadioButton = teacherView.findViewById(R.id.radioButton)

            teacherName.text = course.Tname ?: "Not Assigned"
            val semesterSectionText = "${course.Semester}${course.Section}"
            semesterSection.text = semesterSectionText

            // Check if this teacher is already assigned (from Mapping_review)
            val isAssigned = course.AssignedTeacher != null && course.AssignedTeacher == course.T_ID
            radioButton.isChecked = isAssigned
            radioButton.isEnabled = course.Tname != null

            radioButton.setOnClickListener {
                Log.d("RadioButton", "Radio button clicked")
                if (radioButton.isChecked) {
                    Log.d("RadioButton", "Radio button is checked")

                    // Check if oc_id and T_ID are valid
                    if (course.Oc_id > 0 && !course.T_ID.isNullOrEmpty()) {
                        // Remove all existing selections for this course
                        selectedMappings.removeIf { it.oc_id == course.Oc_id }

                        // Add the new selection
                        selectedMappings.add(Mapping(course.Oc_id, course.T_ID))

                        // Log the T_ID and oc_id
                        Log.d("RadioButton", "Teacher ID (T_ID): ${course.T_ID}")
                        Log.d("RadioButton", "Offered Course ID (oc_id): ${course.Oc_id}")
                        Log.d("RadioButton", "Current List: $selectedMappings")
                        Log.d("SelectedMappings", "Added: oc_id=${course.Oc_id}, T_ID=${course.T_ID}")
                    } else {
                        Toast.makeText(
                            holder.itemView.context,
                            "Invalid oc_id or T_ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Uncheck other radio buttons in the same container
                    for (i in 0 until holder.teacherContainer.childCount) {
                        val child = holder.teacherContainer.getChildAt(i)
                        val otherRadioButton = child.findViewById<RadioButton>(R.id.radioButton)
                        if (otherRadioButton != radioButton) {
                            otherRadioButton.isChecked = false
                        }
                    }
                } else {
                    // If the radio button is unchecked, remove the mapping for this course
                    selectedMappings.removeIf { it.oc_id == course.Oc_id }
                    Log.d("SelectedMappings", "Removed: oc_id=${course.Oc_id}")
                }
            }

            holder.teacherContainer.addView(teacherView)
        }
    }
    override fun getItemCount(): Int {
        return groupedCourses.size
    }

    fun updateList(newList: List<OfferedCourse>) {
        courses = newList
        notifyDataSetChanged()
    }

    fun getSelectedMappings(): List<Mapping> {
        return selectedMappings.toList()
    }

    data class Mapping(val oc_id: Int, val T_ID: String)
}