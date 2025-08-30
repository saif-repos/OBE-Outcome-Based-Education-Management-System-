package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.R

class CourseInfoAdapter(private val items: List<CourseInfoItem>) :
    RecyclerView.Adapter<CourseInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelTextView: TextView = view.findViewById(R.id.labelTextView)
        val valueTextView: TextView = view.findViewById(R.id.valueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.labelTextView.text = item.label
        holder.valueTextView.text = item.value
    }

    override fun getItemCount() = items.size
}