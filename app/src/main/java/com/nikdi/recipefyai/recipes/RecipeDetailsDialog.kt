package com.nikdi.recipefyai.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.nikdi.recipefyai.databinding.DialogRecipeDetailsBinding
import com.nikdi.recipefyai.R

class RecipeDetailsDialog() : DialogFragment() {

    private var _binding: DialogRecipeDetailsBinding? = null
    private val binding get() = _binding!!

    interface RecipeDetailsListener {
        fun onDetailsEntered(servings: String, units: String)
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
        val previousUnit = arguments?.getString("unit") ?: ""

        binding.servingsInput.setText(previousServings)

        val unitOptions = resources.getStringArray(R.array.measurement_systems)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, unitOptions)
        binding.unitsSpinner.adapter = adapter

        if (previousUnit.isNotEmpty()) {
            val position = unitOptions.indexOf(previousUnit)
            if (position != -1) {
                binding.unitsSpinner.setSelection(position)
            }
        }


        // Validate input before enabling the proceed button
        fun validateInputs() {
            val servingsText = binding.servingsInput.text.toString()
            val unitSelected = binding.unitsSpinner.selectedItem?.toString()?.isNotEmpty() == true
            binding.btnProceed.isEnabled = servingsText.isNotEmpty() && unitSelected
        }

        binding.servingsInput.addTextChangedListener { validateInputs() }
        binding.unitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                validateInputs()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.details_title))
            .setView(binding.root)
            .setNegativeButton(getString(R.string.cancel)) { d, _ -> d.dismiss() }
            .create()

        binding.btnProceed.setOnClickListener {
            val servings = binding.servingsInput.text.toString()
            val unit = binding.unitsSpinner.selectedItem.toString()
            listener?.onDetailsEntered(servings, unit)
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(servings: String?, units: String?): RecipeDetailsDialog {
            val dialog = RecipeDetailsDialog()
            val args = Bundle()
            args.putString("servings", servings)
            args.putString("unit", units)
            dialog.arguments = args
            return dialog
        }
    }
}