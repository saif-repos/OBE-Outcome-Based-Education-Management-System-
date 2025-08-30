package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.PLO
import com.example.obe_mngt_sys.R

class PLOSelectionAdapter(
    private var ploList: List<PLO>,
    private val onPLOSelected: (Int, Boolean) -> Unit // Callback for selection changes
) : RecyclerView.Adapter<PLOSelectionAdapter.PLOViewHolder>(), Filterable {

    private var filteredPLOList: List<PLO> = ploList
    private val selectedPLOs = mutableListOf<Int>() // Track selected PLO IDs

    inner class PLOViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbPLO: CheckBox = view.findViewById(R.id.cbPLO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PLOViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plo_selection, parent, false)
        return PLOViewHolder(view)
    }

    override fun onBindViewHolder(holder: PLOViewHolder, position: Int) {
        val plo = filteredPLOList[position]

        // Set the text and checked state
        holder.cbPLO.text = "${plo.PLOId} - ${plo.Description}"
        holder.cbPLO.isChecked = selectedPLOs.contains(plo.PLOId)

        // Handle checkbox changes
        holder.cbPLO.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPLOs.add(plo.PLOId)
            } else {
                selectedPLOs.remove(plo.PLOId)
            }
            // Notify the callback
            onPLOSelected(plo.PLOId, isChecked)
        }
    }

    override fun getItemCount(): Int = filteredPLOList.size

    // Update the adapter data
    fun updateData(newList: List<PLO>) {
        ploList = newList
        filteredPLOList = newList
        selectedPLOs.clear() // Clear selections when data changes
        notifyDataSetChanged()
    }

    // Implement Filterable interface
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filteredList = if (query.isEmpty()) {
                    ploList
                } else {
                    ploList.filter { plo ->
                        plo.PLOId.toString().contains(query) ||
                                plo.Description.lowercase().contains(query)
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPLOList = results?.values as? List<PLO> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    // Function to get selected PLO IDs
    fun getSelectedPLOs(): List<Int> = selectedPLOs.toList()
}