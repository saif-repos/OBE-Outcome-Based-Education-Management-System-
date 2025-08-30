package com.example.obe_mngt_sys.ACTIVITIES

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.obe_mngt_sys.ADAPTERS.CLOAdapter
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.*
import com.example.obe_mngt_sys.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CLOFragment : Fragment(), CLOAdapter.CLOActionListener {

    private var courseCode: String? = null
    private var courseName: String? = null
    private var section: String? = null
    private var semester: Int? = null
    private var offeredCourseId: Int = -1
    private var teacherId: String = ""
    private var hasPermission: Boolean = false

    private lateinit var cloListView: ListView
    private lateinit var courseNameTextView: TextView
    private lateinit var btnAddClos: Button
    private lateinit var btnImportFromAll: Button
    private lateinit var cloAdapter: CLOAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offeredCourseId = it.getInt("offeredCourseId", -1)
            courseCode = it.getString("courseCode")
            courseName = it.getString("courseName")
            section = it.getString("section")
            semester = it.getInt("semester")
            teacherId = it.getString("teacherId") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_c_l_o, container, false)

        courseNameTextView = view.findViewById(R.id.courseNameTextView)
        cloListView = view.findViewById(R.id.cloListView)
        btnAddClos = view.findViewById(R.id.btnAddClos)
        btnImportFromAll = view.findViewById(R.id.btnImportFromPrevious)

        cloAdapter = CLOAdapter(requireContext(), emptyList(), this, hasPermission)
        cloListView.adapter = cloAdapter

        checkPermissionAndSetupButtons()
        btnAddClos.setOnClickListener { showAddCLODialog() }
        btnImportFromAll.setOnClickListener { showImportFromAllCoursesDialog() }
        fetchCLOs()

        return view
    }
    override fun onEditCLO(clo: CLO) {
        showEditCLODialog(clo)
    }

    override fun onDeleteCLO(clo: CLO) {
        showDeleteConfirmationDialog(clo)
    }


    private fun showEditCLODialog(clo: CLO) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_clo, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText).apply {
            setText(clo.description)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit CLO")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newDescription = descriptionEditText.text.toString().trim()
                if (newDescription.isNotEmpty()) {
                    updateCLO(clo.CLO_ID, newDescription)
                } else {
                    showToast("Description cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(clo: CLO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete CLO")
            .setMessage("Are you sure you want to delete '${clo.description}'?")
            .setPositiveButton("Delete") { _, _ -> deleteCLO(clo.CLO_ID) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCLO(cloId: Int, newDescription: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.updateCLO(
                    cloId,
                    UpdateCLORequest(T_id = teacherId, Description = newDescription)
                )
                withContext(Dispatchers.Main) {
                    showToast(response.message)
                    fetchCLOs()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error updating CLO: ${e.message}")
                }
            }
        }
    }

    private fun deleteCLO(cloId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.deleteCLO(cloId)
                withContext(Dispatchers.Main) {
                    showToast(response.message)
                    fetchCLOs()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error deleting CLO: ${e.message}")
                }
            }
        }
    }

    private fun checkPermissionAndSetupButtons() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.CheckPermission(offeredCourseId, teacherId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            hasPermission = it.Result
                            updateButtonsState(hasPermission)
                            if (!hasPermission) showToast("No permission to modify CLOs")
                        }
                    } else {
                        updateButtonsState(false)
                        showToast("Permission check failed")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    updateButtonsState(false)
                    showToast("Error checking permission")
                }
            }
        }
    }

    private fun updateButtonsState(enabled: Boolean) {
        btnAddClos.isEnabled = enabled
        btnAddClos.alpha = if (enabled) 1f else 0.5f
        btnImportFromAll.isEnabled = enabled
        btnImportFromAll.alpha = if (enabled) 1f else 0.5f
        cloAdapter.updatePermission(enabled)
    }
    private fun fetchCLOs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.GetCLOsByOfferedCourseId(offeredCourseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { clos ->
                            val sectionWithoutSemester = section?.substring(1) ?: ""
                            courseNameTextView.text = "$courseName-${semester}$sectionWithoutSemester"
                            cloAdapter = CLOAdapter(requireContext(), clos, this@CLOFragment, hasPermission)
                            cloListView.adapter = cloAdapter
                        } ?: showToast("No CLOs found")
                    } else {
                        showToast("Failed to fetch CLOs: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error fetching CLOs: ${e.message}")
                }
            }
        }
    }

    private fun showAddCLODialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_clo, null)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New CLO")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                descriptionEditText.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
                    addCLO(it)
                } ?: showToast("Description cannot be empty")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addCLO(description: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = CLORequest(
                    ocId = offeredCourseId,
                    teacherId = teacherId,
                    description = description
                )
                val response = RetrofitInstance.apiService.CreateCLO(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            showToast(it.message)
                            fetchCLOs()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        showToast("Error: ${errorBody ?: "Unknown error"}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Exception: ${e.message}")
                }
            }
        }
    }

    private fun showImportFromAllCoursesDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.apiService.getCLOsNotInCourse(offeredCourseId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { clos ->
                            if (clos.isNotEmpty()) {
                                showEnhancedCLOSelectionDialog(clos)
                            } else {
                                showToast("No available CLOs to import")
                            }
                        }
                    } else {
                        showToast("Failed to fetch CLOs")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun showEnhancedCLOSelectionDialog(clos: List<UnmappedCLO>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_clo_selection, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchView)
        val listView = dialogView.findViewById<ListView>(R.id.cloSelectionListView)

        // Create a mutable copy of the original list for filtering
        val filteredList = clos.toMutableList()

        // Create a custom adapter
        val adapter = object : BaseAdapter() {
            private var currentList = clos

            fun updateList(newList: List<UnmappedCLO>) {
                currentList = newList
                notifyDataSetChanged()
            }

            override fun getCount(): Int = currentList.size
            override fun getItem(position: Int): UnmappedCLO = currentList[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_clo_selection, parent, false)

                val clo = getItem(position)

                view.findViewById<TextView>(R.id.cloIdText).text = "CLO${clo.cloId}"
                view.findViewById<TextView>(R.id.cloDescriptionText).text = clo.description
                view.findViewById<TextView>(R.id.teacherIdText).text = "Teacher: ${clo.teacherId}"

                return view
            }
        }

        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase() ?: ""
                if (query.isEmpty()) {
                    adapter.updateList(clos)
                } else {
                    val filtered = clos.filter { clo ->
                        clo.description.lowercase().contains(query) ||
                                clo.cloId.toString().contains(query) ||
                                clo.teacherId.lowercase().contains(query)
                    }
                    adapter.updateList(filtered)
                }
                return true
            }
        })

        AlertDialog.Builder(requireContext())
            .setTitle("Import CLOs (${clos.size} available)")
            .setView(dialogView)
            .setPositiveButton("Import Selected") { _, _ ->
                val selectedPositions = listView.checkedItemPositions
                val selectedCLOs = mutableListOf<UnmappedCLO>()

                for (i in 0 until selectedPositions.size()) {
                    if (selectedPositions.valueAt(i)) {
                        selectedCLOs.add(adapter.getItem(selectedPositions.keyAt(i)))
                    }
                }

                if (selectedCLOs.isNotEmpty()) {
                    confirmImportSelection(selectedCLOs)
                } else {
                    showToast("No CLOs selected")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmImportSelection(selectedCLOs: List<UnmappedCLO>) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Import")
            .setMessage("Import ${selectedCLOs.size} selected CLO(s) to this course?")
            .setPositiveButton("Import") { _, _ ->
                importSelectedCLOs(selectedCLOs)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importSelectedCLOs(selectedCLOs: List<UnmappedCLO>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (clo in selectedCLOs) {
                    val mappingResponse = RetrofitInstance.apiService.AddCrsClo(
                        CrsCloMappingRequest(
                            ocId = offeredCourseId,
                            cloId = clo.cloId
                        )
                    )

                    if (!mappingResponse.isSuccessful) {
                        throw Exception("Failed to map CLO ID: ${clo.cloId}")
                    }
                }

                withContext(Dispatchers.Main) {
                    showToast("Successfully imported ${selectedCLOs.size} CLO(s)")
                    fetchCLOs()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Import failed: ${e.message}")
                }
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int,
            teacherId: String
        ) = CLOFragment().apply {
            arguments = Bundle().apply {
                putString("courseCode", courseCode)
                putString("courseName", courseName)
                putString("section", section)
                putInt("semester", semester)
                putInt("offeredCourseId", offeredCourseId)
                putString("teacherId", teacherId)
            }
        }
    }
}