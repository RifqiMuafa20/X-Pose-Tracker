package com.rifqidev.x_posetracker.utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.rifqidev.x_posetracker.R

class EditTextDialog(
    private val initialText: String,
    private val onSave: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_edit_text, null)

        val etInput = view.findViewById<EditText>(R.id.etInput)
        etInput.setText(initialText)

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_record_name))
            .setView(view)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val result = etInput.text.toString()
                if (result.isBlank()) {
                    etInput.error = getString(R.string.name_empty)
                } else {
                    onSave(result)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }
}