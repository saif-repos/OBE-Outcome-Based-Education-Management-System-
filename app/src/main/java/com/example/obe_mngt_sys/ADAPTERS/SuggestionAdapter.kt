package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.Suggestion
import com.example.obe_mngt_sys.R

class SuggestionAdapter(private var suggestions: List<Suggestion>) :
    RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val suggestionText: TextView = itemView.findViewById(R.id.suggestionText)
    }

    fun updateSuggestions(newSuggestions: List<Suggestion>) {
        this.suggestions = newSuggestions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.suggestionText.text = suggestions[position].suggest
    }

    override fun getItemCount() = suggestions.size
}