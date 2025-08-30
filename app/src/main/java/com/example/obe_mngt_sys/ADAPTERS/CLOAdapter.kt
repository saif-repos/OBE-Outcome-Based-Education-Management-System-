package com.example.obe_mngt_sys.ADAPTERS

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import com.example.obe_mngt_sys.MODELS.CLO
import com.example.obe_mngt_sys.R

class CLOAdapter(
    context: Context,
    private val clos: List<CLO>,
    private val actionListener: CLOActionListener,
    private var hasPermission: Boolean = false
) : ArrayAdapter<CLO>(context, R.layout.item_clo, clos) {

    interface CLOActionListener {
        fun onEditCLO(clo: CLO)
        fun onDeleteCLO(clo: CLO)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_clo, parent, false)

        val clo = getItem(position) ?: return view

        view.findViewById<TextView>(R.id.cloCode).text = "CLO ${clo.displayCloId}"
        view.findViewById<TextView>(R.id.cloDescription).text = clo.description

        val btnEdit = view.findViewById<ImageButton>(R.id.btnEditCLO)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteCLO)

        if (hasPermission) {
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE

            btnEdit.setOnClickListener { actionListener.onEditCLO(clo) }
            btnDelete.setOnClickListener { actionListener.onDeleteCLO(clo) }
        } else {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }

        return view
    }

    override fun getItem(position: Int): CLO {
        return clos[position]
    }

    fun updatePermission(newPermission: Boolean) {
        if (hasPermission != newPermission) {
            hasPermission = newPermission
            notifyDataSetChanged()
        }
    }
}