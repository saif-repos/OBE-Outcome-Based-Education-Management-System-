package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.PLO
import com.example.obe_mngt_sys.R

class PLOAdapter(
    private var ploList: List<PLO>,
    private val actionListener: PLOActionListener
) : RecyclerView.Adapter<PLOAdapter.PLOViewHolder>() {

    interface PLOActionListener {
        fun onEditPLO(plo: PLO)
        fun onDeletePLO(plo: PLO)
    }

    fun updateData(newList: List<PLO>) {
        ploList = newList
        notifyDataSetChanged()
    }

    inner class PLOViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPloId: TextView = view.findViewById(R.id.tvPloId)
        val tvPloDescription: TextView = view.findViewById(R.id.tvPloDescription)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditPLO)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeletePLO)

        init {
            btnEdit.setOnClickListener {
                actionListener.onEditPLO(ploList[adapterPosition])
            }

            btnDelete.setOnClickListener {
                actionListener.onDeletePLO(ploList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PLOViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plo, parent, false)
        return PLOViewHolder(view)
    }

    override fun onBindViewHolder(holder: PLOViewHolder, position: Int) {
        val plo = ploList[position]
        holder.tvPloId.text = "PLO ${plo.PLOId}"
        holder.tvPloDescription.text = plo.Description
    }

    override fun getItemCount(): Int = ploList.size
}