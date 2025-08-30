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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.ADAPTERS.PLOAdapter
import com.example.obe_mngt_sys.ADAPTERS.PLOSelectionAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.*
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.FragmentPlosBinding
import kotlinx.coroutines.launch
import retrofit2.Response

class FragmentPLOs : Fragment(), PLOAdapter.PLOActionListener {

    private var _binding: FragmentPlosBinding? = null
    private val binding get() = _binding!!
    private var programId: Int? = null
    private lateinit var ploAdapter: PLOAdapter
    private val selectedPLOs = mutableListOf<Int>() // List to store selected PLO IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            programId = it.getInt("PROGRAM_ID")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ploAdapter = PLOAdapter(emptyList(), this)
        binding.recyclerViewPlos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ploAdapter
        }

        programId?.let { fetchPLOs(it) } ?: showToast("Invalid Program ID")
        binding.btnAddPlos.setOnClickListener { showAddPLODialog() }
    }

    override fun onEditPLO(plo: PLO) {
        showEditPLODialog(plo)
    }

    override fun onDeletePLO(plo: PLO) {
        showDeleteConfirmationDialog(plo)
    }

    private fun showEditPLODialog(plo: PLO) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_plo, null)
        val etPLODescription = dialogView.findViewById<EditText>(R.id.etPLODescription).apply {
            setText(plo.Description)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit PLO")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newDescription = etPLODescription.text.toString().trim()
                if (newDescription.isNotEmpty()) {
                    updatePLO(plo.PLOId, newDescription)
                } else {
                    showToast("Description cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(plo: PLO) {
        programId?.let { pId ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete PLO")
                .setMessage("Are you sure you want to delete '${plo.Description}' from this program?")
                .setPositiveButton("Delete") { _, _ -> deletePLO(plo.PLOId, pId) }
                .setNegativeButton("Cancel", null)
                .show()
        } ?: showToast("Program ID not available")
    }

    private fun updatePLO(ploId: Int, newDescription: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.updatePLO(
                    ploId,
                    UpdatePLORequest(newDescription)
                )
                showToast(response.message ?: "PLO updated successfully")
                programId?.let { fetchPLOs(it) }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun deletePLO(ploId: Int, pId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.deletePLO(ploId, pId)
                showToast(response.message ?: "PLO deleted successfully")
                fetchPLOs(pId)
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showAddPLODialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_plo, null)
        val etPLODescription = dialogView.findViewById<EditText>(R.id.etPLODescription)
        val btnSavePLO = dialogView.findViewById<Button>(R.id.btnSavePLO)
        val btnAddFromPrevious = dialogView.findViewById<Button>(R.id.btnAddFromPrevious)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add PLO")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        btnSavePLO.setOnClickListener {
            val description = etPLODescription.text.toString().trim()
            if (description.isNotEmpty()) {
                programId?.let { addPLO(description, it) }
                dialog.dismiss()
            } else {
                showToast("Please enter a description")
            }
        }

        btnAddFromPrevious.setOnClickListener {
            selectedPLOs.clear() // Clear previous selections
            programId?.let { fetchPLOsNotInProgram(it) }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addPLO(description: String, programId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.addPLO(AddPLORequest(description, programId))
                showToast("PLO added successfully: ${response.Description}")
                fetchPLOs(programId)
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchPLOs(programId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.getPLOsByProgramId(programId)
                if (response.isSuccessful) {
                    response.body()?.let { plos ->
                        ploAdapter.updateData(plos)
                    } ?: showToast("No PLOs found")
                } else {
                    showToast("Failed to load PLOs")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchPLOsNotInProgram(programId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.getPLOsNotInProgram(programId)
                if (response.isNotEmpty()) {
                    showPLOsSelectionDialog(response)
                } else {
                    showToast("No additional PLOs found to add.")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showPLOsSelectionDialog(plosList: List<PLO>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_course_selection, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Select PLOs to Add")
            .setPositiveButton("Save") { dialog, _ ->
                programId?.let { addSelectedPLOsToProgram(it, selectedPLOs) }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCourses)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = PLOSelectionAdapter(plosList) { ploId, isChecked ->
            if (isChecked) {
                selectedPLOs.add(ploId)
            } else {
                selectedPLOs.remove(ploId)
            }
        }
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        dialog.show()
    }

    private fun addSelectedPLOsToProgram(programId: Int, selectedPLOIds: List<Int>) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.apiService.addSelectedPLOsToProgram(programId, selectedPLOIds)
                if (response.isSuccessful) {
                    showToast("PLOs added successfully!")
                    fetchPLOs(programId)
                } else {
                    showToast("Failed to add PLOs")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(programId: Int) = FragmentPLOs().apply {
            arguments = Bundle().apply {
                putInt("PROGRAM_ID", programId)
            }
        }
    }
}