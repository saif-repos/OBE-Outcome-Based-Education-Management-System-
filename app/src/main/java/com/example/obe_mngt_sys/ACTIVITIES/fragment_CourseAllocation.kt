package com.example.obe_mngt_sys

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.CourseAllocationAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.OfferedCourse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentCourseAllocation : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAllocationAdapter
    private lateinit var searchBar: EditText
    private lateinit var saveButton: Button
    private var originalCourseList: List<OfferedCourse> = emptyList()
    private var programId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment__course_allocation, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewCourseAlloc)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize Search Bar
        searchBar = view.findViewById(R.id.searchBar)

        // Initialize Save Button
        saveButton = view.findViewById(R.id.saveButton)

        // Get programId from arguments
        programId = arguments?.getInt("PROGRAM_ID") ?: -1

        // Initialize adapter
        adapter = CourseAllocationAdapter(emptyList())
        recyclerView.adapter = adapter

        // Fetch data from API
        fetchCourseAllocations()

        // Set up search functionality
        setupSearchBar()

        // Set up save button click listener
        saveButton.setOnClickListener {
            saveSelectedTeachers()
        }

        return view
    }

    private fun fetchCourseAllocations() {
        if (programId == -1) {
            Toast.makeText(requireContext(), "Invalid Program ID", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.apiService.getOfferedCourses(programId)
                if (response.isSuccessful) {
                    val offeredCourses = response.body()

                    // Debug: Log the raw API response
                    Log.d("API Response", offeredCourses.toString())

                    if (offeredCourses != null) {
                        // Debug: Print the fetched data
                        offeredCourses.forEach { course ->
                            Log.d("OfferedCourse", "oc_id: ${course.Oc_id}, T_ID: ${course.T_ID}")
                        }

                        originalCourseList = offeredCourses
                        adapter.updateList(offeredCourses)
                    } else {
                        Toast.makeText(requireContext(), "No data found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch data: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCourses(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterCourses(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalCourseList
        } else {
            originalCourseList.filter { course ->
                course.C_code.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filteredList)
    }

    private fun saveSelectedTeachers() {
        val selectedMappings = adapter.getSelectedMappings()
        if (selectedMappings.isEmpty()) {
            Toast.makeText(requireContext(), "No teachers selected", Toast.LENGTH_SHORT).show()
            return
        }

        // Debug: Print selectedMappings
        selectedMappings.forEach { mapping ->
            Log.d("SelectedMappings", "oc_id: ${mapping.oc_id}, T_ID: ${mapping.T_ID}")
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                selectedMappings.forEach { mapping ->
                    // Debug: Print oc_id and T_ID
                    println("oc_id: ${mapping.oc_id}, T_ID: ${mapping.T_ID}")

                    if (mapping.oc_id > 0 && mapping.T_ID.isNotEmpty()) {
                        val response = RetrofitInstance.apiService.allocateTeacher(mapping.oc_id, mapping.T_ID)
                        if (response.isSuccessful) {
                            val allocationResponse = response.body()
                            if (allocationResponse != null) {
                                Toast.makeText(
                                    requireContext(),
                                    allocationResponse.Message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to allocate teacher: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Invalid oc_id or T_ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
    companion object {
        fun newInstance(programId: Int) = FragmentCourseAllocation().apply {
            arguments = Bundle().apply {
                putInt("PROGRAM_ID", programId)
            }
        }
    }
}