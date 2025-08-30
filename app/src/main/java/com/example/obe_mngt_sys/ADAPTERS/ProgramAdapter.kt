package com.example.obe_mngt_sys.ADAPTERS

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.obe_mngt_sys.ACTIVITIES.Program_courses
import com.example.obe_mngt_sys.HELPER.RetrofitInstance
import com.example.obe_mngt_sys.MODELS.DeleteProgResponse
import com.example.obe_mngt_sys.MODELS.PostProgram
import com.example.obe_mngt_sys.MODELS.Program
import com.example.obe_mngt_sys.MODELS.UpdateProgResponse
import com.example.obe_mngt_sys.R

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProgramAdapter(
    private val context: Context,
    private val programs: List<Program>,
    private val hodId: String,
    private val hodName: String,
    private val refreshCallback: () -> Unit
) : ArrayAdapter<Program>(context, R.layout.program_list_item, programs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.program_list_item, parent, false)

        val program = programs[position]
        val programButton = view.findViewById<TextView>(R.id.programButton)
        val editIcon = view.findViewById<ImageView>(R.id.editIcon)
        val deleteIcon = view.findViewById<ImageView>(R.id.deleteIcon)

        programButton.text = program.ProgramName

        // Handle program click (navigate to courses)
        programButton.setOnClickListener {
            val intent = Intent(context, Program_courses::class.java)
            intent.putExtra("HOD_ID", hodId)
            intent.putExtra("HOD_NAME", hodName)
            intent.putExtra("PROGRAM_ID", program.ProgramID)
            intent.putExtra("PROGRAM_NAME", program.ProgramName)
            context.startActivity(intent)
        }

        // Handle edit click
        editIcon.setOnClickListener {
            showEditProgramDialog(program)
        }

        // Handle delete click
        deleteIcon.setOnClickListener {
            showDeleteConfirmationDialog(program)
        }

        return view
    }

    private fun showEditProgramDialog(program: Program) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_program, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextProgramDesc)
        editText.setText(program.ProgramName)

        AlertDialog.Builder(context)
            .setTitle("Edit Program")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateProgram(program.ProgramID, newName)
                } else {
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(program: Program) {
        AlertDialog.Builder(context)
            .setTitle("Delete ${program.ProgramName}?")
            .setMessage("This action cannot be undone")
            .setPositiveButton("Delete") { _, _ ->
                deleteProgram(program.ProgramID)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProgram(programId: Int, newName: String) {
        val apiService = RetrofitInstance.apiService
        val updatedProgram = PostProgram(pname = newName, H_ID = hodId)

        apiService.updateProgram(programId.toString(), updatedProgram)
            .enqueue(object : Callback<UpdateProgResponse> {
                override fun onResponse(
                    call: Call<UpdateProgResponse>,
                    response: Response<UpdateProgResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            response.body()?.message ?: "Program updated!",
                            Toast.LENGTH_SHORT
                        ).show()
                        refreshCallback()
                    } else {
                        Toast.makeText(
                            context,
                            "Update failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UpdateProgResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun deleteProgram(programId: Int) {
        val apiService = RetrofitInstance.apiService

        apiService.deleteProgram(programId.toString())
            .enqueue(object : Callback<DeleteProgResponse> {
                override fun onResponse(
                    call: Call<DeleteProgResponse>,
                    response: Response<DeleteProgResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            response.body()?.message ?: "Program deleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        refreshCallback()
                    } else {
                        Toast.makeText(
                            context,
                            "Delete failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeleteProgResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}