package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.R

class MarksDistributionAdapter(
    private var items: List<MarksItem>,
    private val onAddButtonClick: (MarksItem) -> Unit
) : RecyclerView.Adapter<MarksDistributionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityName: TextView = view.findViewById(R.id.activityNameTextView)
        val marksValue: TextView = view.findViewById(R.id.marksValueTextView)
        val addButton: Button = view.findViewById(R.id.addButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marks_distribution, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.activityName.text = item.name
        holder.marksValue.text = item.value.toString()

        holder.addButton.visibility = if (item.showAddButton) View.VISIBLE else View.GONE
        holder.addButton.setOnClickListener {
            if (item.showAddButton) {
                onAddButtonClick(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<MarksItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    data class MarksItem(val name: String, val value: Int, val showAddButton: Boolean)
}