package com.nikdi.recipefyai.logicrel

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.databinding.DialogRecipeDetailsBinding
import com.nikdi.recipefyai.R

class RecipeDetailsDialog : DialogFragment() {

    private var _binding: DialogRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    interface RecipeDetailsListener {
        fun onProceed(servings: String, portionSize: String)
        fun onCancel(servings: String, portionSize: String)
    }

    private var listener: RecipeDetailsListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            parentFragment as? RecipeDetailsListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RecipeDetailsListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRecipeDetailsBinding.inflate(layoutInflater)

        val previousServings = arguments?.getString("servings") ?: ""
        val previousPortionSize = arguments?.getString("portionSize") ?: ""

        binding.servingsInput.setText(previousServings)
        binding.portionSizeInput.setText(previousPortionSize)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnProceed.setOnClickListener { view ->
            val servings = binding.servingsInput.text.toString()
            val portionSize = binding.portionSizeInput.text.toString()
            if (validateInputs(view, servings, portionSize)) {
                listener?.onProceed(servings, portionSize)
                dismiss()
            }
        }

        return dialog
    }

    private fun validateInputs(view: View, servings: String, portionSize: String): Boolean {
        if (servings.isEmpty() || servings.toInt() <= 0) {
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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val servings = binding.servingsInput.text.toString()
        val portionSize = binding.portionSizeInput.text.toString()
        listener?.onCancel(servings, portionSize)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(servings: String?, portionSize: String?): RecipeDetailsDialog {
            return RecipeDetailsDialog().apply {
                arguments = Bundle().apply {
                    putString("servings", servings)
                    putString("portionSize", portionSize)
                }
            }
        }
    }
}