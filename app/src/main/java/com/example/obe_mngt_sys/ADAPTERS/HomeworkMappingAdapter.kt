package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.R

class HomeworkMappingAdapter(
    private var items: List<HomeworkItem>,
    private val onHomeworkUpdated: (HomeworkItem) -> Unit
) : RecyclerView.Adapter<HomeworkMappingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quiz: EditText = view.findViewById(R.id.quizEditText)
        val assignments: EditText = view.findViewById(R.id.assignmentsEditText)
        val others: EditText = view.findViewById(R.id.othersEditText)
        val total: TextView = view.findViewById(R.id.totalTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_homework_mapping, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.quiz.setText(item.quiz.toString())
        holder.assignments.setText(item.assignments.toString())
        holder.others.setText(item.others.toString())
        holder.total.text = item.total.toString()

        // Add text change listeners
        holder.quiz.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateItem(holder, item)
            }
        }

        holder.assignments.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateItem(holder, item)
            }
        }

        holder.others.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateItem(holder, item)
            }
        }
    }

    private fun updateItem(holder: ViewHolder, originalItem: HomeworkItem) {
        try {
            val quiz = holder.quiz.text.toString().toFloat()
            val assignments = holder.assignments.text.toString().toFloat()
            val others = holder.others.text.toString().toFloat()
            val total = quiz + assignments + others

            val updatedItem = originalItem.copy(
                quiz = quiz,
                assignments = assignments,
                others = others,
                total = total
            )

            holder.total.text = total.toString()
            onHomeworkUpdated(updatedItem)
        } catch (e: NumberFormatException) {
            // Reset to original values if invalid input
            holder.quiz.setText(originalItem.quiz.toString())
            holder.assignments.setText(originalItem.assignments.toString())
            holder.others.setText(originalItem.others.toString())
            holder.total.text = originalItem.total.toString()
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<HomeworkItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    data class HomeworkItem(
        val quiz: Float,
        val assignments: Float,
        val others: Float,
        val total: Float
    )
}