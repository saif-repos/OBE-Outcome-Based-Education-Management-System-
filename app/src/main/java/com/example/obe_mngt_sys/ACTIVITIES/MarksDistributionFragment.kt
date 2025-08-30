package com.example.obe_mngt_sys.ACTIVITIES

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.obe_mngt_sys.ADAPTERS.HomeworkMappingAdapter
import com.example.obe_mngt_sys.ADAPTERS.MarksDistributionAdapter
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.FragmentMarksDistributionBinding
import com.example.obe_mngt_sys.MODELS.*
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

class MarksDistributionFragment : Fragment() {

    private var _binding: FragmentMarksDistributionBinding? = null
    private val binding get() = _binding!!

    private var offeredCourseId: Int = -1
    private lateinit var marksDistributionAdapter: MarksDistributionAdapter
    private lateinit var homeworkMappingAdapter: HomeworkMappingAdapter

    private var currentMarksDistribution: MarksDistributionResponse? = null
    private var currentHomework: HomeworkResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            offeredCourseId = it.getInt("offeredCourseId", -1)
            Log.d("MarksDistributionFragment", "Offered Course ID: $offeredCourseId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarksDistributionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupClickListeners()
        loadData()

        // Initially hide homework mapping section
        binding.homeworkMappingSection.visibility = View.GONE
    }

    private fun setupRecyclerViews() {
        // Marks Distribution RecyclerView (non-editable)
        marksDistributionAdapter = MarksDistributionAdapter(emptyList()) { marksItem ->
            if (marksItem.name == "HW") {
                toggleHomeworkMappingVisibility(true)
                loadHomeworkMapping()
            }
        }
        binding.marksDistributionRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = marksDistributionAdapter
            setHasFixedSize(true)
        }

        // Homework Mapping RecyclerView (editable)
        homeworkMappingAdapter = HomeworkMappingAdapter(emptyList()) { updatedHomework ->
            currentHomework = HomeworkResponse(
                Quiz = updatedHomework.quiz,
                Assignment = updatedHomework.assignments,
                Other = updatedHomework.others,
                Exists = true
            )
        }
        binding.homeworkMappingRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = homeworkMappingAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.closeHomeworkButton.setOnClickListener {
            toggleHomeworkMappingVisibility(false)
        }

        binding.updateButton.setOnClickListener {
            saveHomeworkMapping()
        }
    }

    private fun toggleHomeworkMappingVisibility(show: Boolean) {
        if (show) {
            binding.homeworkMappingSection.apply {
                visibility = View.VISIBLE
                alpha = 0f
                animate().alpha(1f).setDuration(300).start()
            }
        } else {
            binding.homeworkMappingSection.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    binding.homeworkMappingSection.visibility = View.GONE
                }
                .start()
        }
    }

    private fun loadData() {
        if (offeredCourseId == -1) {
            Log.e("MarksDistributionFragment", "Invalid offeredCourseId")
            return
        }
        loadMarksDistribution()
    }

    private fun loadMarksDistribution() {
        lifecycleScope.launch {
            try {
                val response: Response<MarksDistributionResponse> =
                    RetrofitInstance.apiService.getMarksDistribution(offeredCourseId)

                if (response.isSuccessful) {
                    response.body()?.let { marksData ->
                        currentMarksDistribution = marksData
                        updateMarksDistributionUI(marksData)
                    }
                } else {
                    Log.e("MarksDistribution", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("MarksDistribution", "Exception: ${e.message}")
            }
        }
    }

    private fun loadHomeworkMapping() {
        lifecycleScope.launch {
            try {
                val response: Response<HomeworkResponse> =
                    RetrofitInstance.apiService.getHomeworkByOcId(offeredCourseId)

                if (response.isSuccessful) {
                    response.body()?.let { homeworkData ->
                        currentHomework = homeworkData
                        updateHomeworkMappingUI(homeworkData)
                    }
                } else {
                    Log.e("HomeworkMapping", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeworkMapping", "Exception: ${e.message}")
            }
        }
    }

    private fun updateMarksDistributionUI(data: MarksDistributionResponse) {
        val items = listOf(
            MarksDistributionAdapter.MarksItem("HW", data.hw, true),
            MarksDistributionAdapter.MarksItem("MID", data.mid, false),
            MarksDistributionAdapter.MarksItem("FINAL", data.final, false),
            MarksDistributionAdapter.MarksItem("LAB", data.lab, false)
        )
        marksDistributionAdapter.updateItems(items)
        binding.grandTotalTextView.text = data.gtotal.toString()
    }

    private fun updateHomeworkMappingUI(data: HomeworkResponse) {
        val total = data.Quiz + data.Assignment + data.Other
        val items = listOf(
            HomeworkMappingAdapter.HomeworkItem(
                data.Quiz,
                data.Assignment,
                data.Other,
                total
            )
        )
        homeworkMappingAdapter.updateItems(items)
    }

    private fun saveHomeworkMapping() {
        val marks = currentMarksDistribution ?: return
        val homework = currentHomework ?: HomeworkResponse(0f, 0f, 0f, false)

        // Validate homework values sum up to HW marks
        val homeworkSum = homework.Quiz + homework.Assignment + homework.Other
        if (homeworkSum != marks.hw.toFloat()) {
            Toast.makeText(
                context,
                "Homework components must sum up to ${marks.hw} (Current: $homeworkSum)",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val request = MarksDistributionRequest(
            oc_id = offeredCourseId,
            hw = marks.hw,
            mid = marks.mid,
            final = marks.final,
            lab = marks.lab,
            gtotal = marks.gtotal,
            qp = marks.qp,
            quiz = homework.Quiz,
            assignment = homework.Assignment,
            other = homework.Other
        )

        lifecycleScope.launch {
            try {
                val response: Response<md_respone> =
                    RetrofitInstance.apiService.updateMarksDistribution(request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Homework mapping updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    toggleHomeworkMappingVisibility(false)
                } else {
                    Toast.makeText(
                        context,
                        "Failed to update: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            courseCode: String,
            courseName: String,
            section: String,
            semester: Int,
            offeredCourseId: Int
        ): MarksDistributionFragment {
            return MarksDistributionFragment().apply {
                arguments = Bundle().apply {
                    putString("courseCode", courseCode)
                    putString("courseName", courseName)
                    putString("section", section)
                    putInt("semester", semester)
                    putInt("offeredCourseId", offeredCourseId)
                }
            }
        }
    }
}