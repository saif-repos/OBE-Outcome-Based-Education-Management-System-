package com.example.obe_mngt_sys.ACTIVITIES

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.CoursesAdapter
import com.example.obe_mngt_sys.ADAPTERS.CoursesSelectionAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.CourseResponse
import com.example.obe_mngt_sys.MODELS.Courses
import com.example.obe_mngt_sys.MODELS.SelectedCoursesRequest
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.FragmentCoursesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentCourses : Fragment() {
    private var programId: Int = -1
    private lateinit var binding: FragmentCoursesBinding
    private val selectedCourses = mutableListOf<String>()
    private lateinit var originalCoursesList: List<Courses>
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            programId = it.getInt(ARG_PROGRAM_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()

        if (programId != -1) {
            fetchCourses(programId)
        }

        binding.btnAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearchView() {
        searchView = binding.root.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCourses(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterCourses(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalCoursesList
        } else {
            originalCoursesList.filter { course ->
                course.CourseId.contains(query, ignoreCase = true) ||
                        course.CourseName.contains(query, ignoreCase = true)
            }
        }
        (binding.recyclerViewCourses.adapter as? CoursesAdapter)?.updateCourses(filteredList)
    }

    private fun fetchCourses(programId: Int) {
        RetrofitInstance.apiService.getCoursesByProgramId(programId).enqueue(object : Callback<List<Courses>> {
            override fun onResponse(call: Call<List<Courses>>, response: Response<List<Courses>>) {
                if (response.isSuccessful) {
                    response.body()?.let { courses ->
                        originalCoursesList = courses
                        if (courses.isNotEmpty()) {
                            setupCourseAdapter(courses)
                        } else {
                            showEmptyState()
                        }
                    }
                } else {
                    showError("Failed to load courses: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Courses>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun setupCourseAdapter(courses: List<Courses>) {
        binding.recyclerViewCourses.adapter = CoursesAdapter(
            courses,
            onEditClick = { course ->
                showEditCourseDialog(course)
            },
            onDeleteClick = { courseCode ->
                showDeleteConfirmationDialog(courseCode)
            }
        )
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_course, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Course")
            .create()

        setupCourseDialog(dialog, dialogView, null)
    }

    private fun showEditCourseDialog(course: Courses) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_course, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Course")
            .create()

        setupCourseDialog(dialog, dialogView, course)
    }

    private fun setupCourseDialog(dialog: AlertDialog, dialogView: View, course: Courses?) {
        val etCourseCode = dialogView.findViewById<EditText>(R.id.etCourseCode)
        val etCourseName = dialogView.findViewById<EditText>(R.id.etCourseName)
        val etCreditHours = dialogView.findViewById<EditText>(R.id.etCreditHours)
        val etLab = dialogView.findViewById<EditText>(R.id.etLab)
        val etPrerequisite = dialogView.findViewById<EditText>(R.id.etPrerequisite)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnAddFromPrevious = dialogView.findViewById<Button>(R.id.btnAddFromPrevious)

        if (course != null) {
            etCourseCode.setText(course.CourseId)
            etCourseName.setText(course.CourseName)
            etCreditHours.setText(course.CourseHours.toString())
            etLab.setText(course.Lab)
            etPrerequisite.setText(course.Prerequisite)
            etCourseCode.isEnabled = false
            btnAddFromPrevious.visibility = View.GONE
        } else {
            btnAddFromPrevious.setOnClickListener {
                selectedCourses.clear()
                fetchCoursesNotInProgram(programId)
            }
        }

        btnSave.setOnClickListener {
            val courseCode = etCourseCode.text.toString().trim()
            val courseName = etCourseName.text.toString().trim()
            val creditHours = etCreditHours.text.toString().toIntOrNull() ?: 0
            val lab = etLab.text.toString().trim()
            val prerequisite = etPrerequisite.text.toString().trim()

            if (validateCourseInput(courseCode, courseName, lab)) {
                saveCourse(courseCode, courseName, creditHours, lab, prerequisite)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun validateCourseInput(code: String, name: String, lab: String): Boolean {
        if (code.isEmpty() || name.isEmpty() || lab.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveCourse(courseCode: String, courseName: String, creditHours: Int, lab: String, prerequisite: String) {
        RetrofitInstance.apiService.AddCourseToProgram(
            courseCode, courseName, creditHours, lab, programId, prerequisite
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showSuccess("Course saved successfully!")
                    fetchCourses(programId)
                } else {
                    showError("Failed to save course: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun showDeleteConfirmationDialog(courseCode: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Course")
            .setMessage("Are you sure you want to delete this course?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteCourse(courseCode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteCourse(courseCode: String) {
        RetrofitInstance.apiService.deleteCourseFromProgram(courseCode).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showSuccess("Course deleted successfully!")
                    fetchCourses(programId)
                } else {
                    showError("Failed to delete course")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun fetchCoursesNotInProgram(programId: Int) {
        RetrofitInstance.apiService.getCoursesNotInProgram(programId).enqueue(object : Callback<List<CourseResponse>> {
            override fun onResponse(call: Call<List<CourseResponse>>, response: Response<List<CourseResponse>>) {
                if (response.isSuccessful) {
                    response.body()?.let { courses ->
                        if (courses.isNotEmpty()) {
                            showCoursesSelectionDialog(courses)
                        } else {
                            showError("No additional courses available")
                        }
                    }
                } else {
                    showError("Failed to fetch courses: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<CourseResponse>>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun showCoursesSelectionDialog(courses: List<CourseResponse>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_course_selection, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select Courses")
            .setPositiveButton("Add") { _, _ ->
                addSelectedCourses()
            }
            .setNegativeButton("Cancel", null)
            .create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CoursesSelectionAdapter(courses) { courseCode, isChecked ->
            if (isChecked) selectedCourses.add(courseCode) else selectedCourses.remove(courseCode)
        }

        dialog.show()
    }

    private fun addSelectedCourses() {
        if (selectedCourses.isEmpty()) {
            showError("No courses selected")
            return
        }

        RetrofitInstance.apiService.addSelectedCoursesToProgram(
            SelectedCoursesRequest(programId, selectedCourses)
        ).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showSuccess("Courses added successfully!")
                    fetchCourses(programId)
                } else {
                    showError("Failed to add courses: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showError("Error: ${t.message}")
            }
        })
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: "An error occurred", Toast.LENGTH_SHORT).show()
    }

    private fun showEmptyState() {
        Toast.makeText(requireContext(), "No courses found", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_PROGRAM_ID = "PROGRAM_ID"

        fun newInstance(programId: Int): FragmentCourses {
            return FragmentCourses().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PROGRAM_ID, programId)
                }
            }
        }
    }
}