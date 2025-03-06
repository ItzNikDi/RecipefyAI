package com.nikdi.recipefyai.logicrel

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.databinding.DialogFromNameBinding

class FromNameDialog : DialogFragment() {
    private var _binding: DialogFromNameBinding? = null
    private val binding get() = _binding!!

    interface FromNameListener {
        fun onProceed(name: String, servings: String, portionSize: String)
    }

    private var listener: FromNameListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            parentFragment as? FromNameListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement FromNameListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFromNameBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnProceed.setOnClickListener { view ->
            val recipeName = binding.recipeNameInput.text.toString()
            val servings = binding.servingsInput.text.toString()
            val portionSize = binding.portionSizeInput.text.toString()
            if (validateInputs(view, recipeName, servings, portionSize)) {
                listener?.onProceed(recipeName, servings, portionSize)
                dismiss()
            }
        }

        return dialog
    }


    private fun validateInputs(view: View, name: String, servings: String, portionSize: String): Boolean {
        if (name.isEmpty()) {
            Snackbar.make(view, getString(R.string.recipe_name_empty), Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.btnProceed)
                .show()
            return false
        }
        else if (servings.isEmpty() || servings.toInt() <= 0) {
            Snackbar.make(view, getString(R.string.servings_empty), Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.btnProceed)
                .show()
            return false
        } else if (portionSize.isEmpty() || portionSize.toFloat() <= 0) {
            Snackbar.make(view, getString(R.string.portion_size_empty), Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.btnProceed)
                .show()
            return false
        } else return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}