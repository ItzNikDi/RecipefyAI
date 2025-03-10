package com.nikdi.recipefyai.logicrel

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.databinding.DialogRecipeNameBinding

class RecipeNameDialog : DialogFragment() {
    private var _binding: DialogRecipeNameBinding? = null
    private val binding get() = _binding!!

    interface RecipeNameListener {
        fun onSaveRecipe(name: String)
        fun onCancel(name: String)
    }

    private var listener: RecipeNameListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            parentFragment as? RecipeNameListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement RecipeNameListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRecipeNameBinding.inflate(layoutInflater)

        val suggestedName = arguments?.getString("suggestedName")
        binding.recipeNameInput.setText(suggestedName)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnProceed.setOnClickListener { view ->
            val selectedName = binding.recipeNameInput.text.toString()
            if (validateName(view, selectedName)) {
                listener?.onSaveRecipe(selectedName)
                dismiss()
            }
        }

        return dialog
    }


    private fun validateName(view: View, name: String): Boolean {
        if (name.isEmpty()) {
            Snackbar.make(view, getString(R.string.recipe_name_empty), Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.btnProceed)
                .show()
            return false
        } else return true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val selectedName = binding.recipeNameInput.text.toString()
        listener?.onCancel(selectedName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(name: String?): RecipeNameDialog {
            return RecipeNameDialog().apply {
                arguments = Bundle().apply {
                    putString("suggestedName", name)
                }
            }
        }
    }
}