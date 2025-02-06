package com.nikdi.recipefyai.recipes

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.nikdi.recipefyai.R

class RecipeMethodDialog : DialogFragment() {

    interface RecipeMethodListener {
        fun onTextChosen()
        fun onPhotoChosen()
    }

    private var listener: RecipeMethodListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? RecipeMethodListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_method, null)
        dialog.setContentView(view)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background

        view.findViewById<Button>(R.id.btn_choose_text).setOnClickListener {
            listener?.onTextChosen()
            dismiss()
        }

        view.findViewById<Button>(R.id.btn_choose_photo).setOnClickListener {
            listener?.onPhotoChosen()
            dismiss()
        }

        return dialog
    }
}
