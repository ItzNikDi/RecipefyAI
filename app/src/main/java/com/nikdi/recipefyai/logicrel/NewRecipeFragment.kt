package com.nikdi.recipefyai.logicrel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nikdi.recipefyai.MainActivity
import com.nikdi.recipefyai.databinding.FragmentNewRecipeBinding

class NewRecipeFragment : Fragment(), FromNameDialog.FromNameListener {
    private var _binding: FragmentNewRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).setUpActionBarForFragment(this)
        _binding = FragmentNewRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnFromPhoto.setOnClickListener {
            openGallery()
        }

        binding.btnFromText.setOnClickListener {
            val action = NewRecipeFragmentDirections.actionNewRecipeFragmentToIngredientSelectionFragment(null)
            findNavController().navigate(action)
        }

        binding.btnFromName.setOnClickListener {
            val dialog = FromNameDialog()
            dialog.show(childFragmentManager, "FromNameDialog")
        }

        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    val action = NewRecipeFragmentDirections.actionNewRecipeFragmentToIngredientSelectionFragment(imageUri.toString())
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onProceed(name: String, servings: String, portionSize: String) {
        val action = NewRecipeFragmentDirections
            .actionNewRecipeFragmentToTemporaryRecipeFragment(
                ingredients = null,
                name,
                servings.toInt(),
                portionSize.toFloat())
        findNavController().navigate(action)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
