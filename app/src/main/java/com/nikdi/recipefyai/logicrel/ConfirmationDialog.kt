package com.nikdi.recipefyai.logicrel

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.nikdi.recipefyai.databinding.DialogConfirmBinding

class ConfirmationDialog(private val onConfirm: () -> Unit) : DialogFragment() {
    private var _binding: DialogConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfirmBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnProceed.setOnClickListener {
            onConfirm.invoke()
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}