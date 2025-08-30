package com.example.obe_mngt_sys.ADAPTERS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obe_mngt_sys.MODELS.PLOResponse
import com.example.obe_mngt_sys.R

class PloAdaptertwo(private val plos: List<PLOResponse>) :
    RecyclerView.Adapter<PloAdaptertwo.PloViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plo, parent, false)
        return PloViewHolder(view)
    }

    override fun onBindViewHolder(holder: PloViewHolder, position: Int) {
        val plo = plos.getOrNull(position) ?: return
        holder.bind(plo, position)
    }

    override fun getItemCount(): Int = plos.size

    class PloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val letterText: TextView = itemView.findViewById(R.id.letterText)
        private val descText: TextView = itemView.findViewById(R.id.descText)

        fun bind(plo: PLOResponse, position: Int) {
            letterText.text = "${('a' + position)}."
            descText.text = plo.description ?: "PLO Description Not Available"
        }
    }
}