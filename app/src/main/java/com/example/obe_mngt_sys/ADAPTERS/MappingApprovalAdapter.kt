package com.example.obe_mngt_sys.ADAPTERS

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.TeacherMappingResponse
import com.example.obe_mngt_sys.R
import com.example.obe_mngt_sys.databinding.ItemMappingApprovalBinding

class MappingApprovalAdapter(
    private val onViewClick: (TeacherMappingResponse) -> Unit
) : ListAdapter<TeacherMappingResponse, MappingApprovalAdapter.MappingViewHolder>(DiffCallback()) {

    inner class MappingViewHolder(private val binding: ItemMappingApprovalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TeacherMappingResponse) {
            binding.apply {
                // Set course and teacher information
                textCourseName.text = item.courseName
                textTeacherName.text = item.teacherName

                // Set status with color coding
                textStatus.text = item.status.uppercase()
                when (item.status.lowercase()) {
                    "approved" -> {
                        textStatus.setTextColor(Color.GREEN)
                        textStatus.setBackgroundResource(R.drawable.bg_status_approved)
                    }
                    "suggested" -> {
                        textStatus.setTextColor(Color.RED)
                        textStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    }
                    else -> { // pending
                        textStatus.setTextColor(Color.parseColor("#FFA500")) // Orange
                        textStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    }
                }

                // Set click listener for view button
                buttonView.setOnClickListener {
                    onViewClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MappingViewHolder {
        val binding = ItemMappingApprovalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MappingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MappingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TeacherMappingResponse>() {
        override fun areItemsTheSame(
            oldItem: TeacherMappingResponse,
            newItem: TeacherMappingResponse
        ): Boolean {
            return oldItem.offeredCourseId == newItem.offeredCourseId
        }

        override fun areContentsTheSame(
            oldItem: TeacherMappingResponse,
            newItem: TeacherMappingResponse
        ): Boolean {
            return oldItem == newItem
        }
    }
}