package com.nikdi.recipefyai.recipes

package com.nikdi.recipefyai.recipes

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
import com.nikdi.recipefyai.databinding.FragmentNewRecipeBinding
import com.nikdi.recipefyai.R

class RecipeManagementFragment : Fragment(), RecipeMethodDialog.RecipeMethodListener {
    private var _binding: FragmentNewRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Button: Choose Recipe Method
        binding.btnNewRecipe.setOnClickListener {
            // Show dialog to choose whether the user wants a recipe via photo or text
            RecipeMethodDialog().show(parentFragmentManager, "choose_method")
        }

        // Button: Create Recipe from Photo
        binding.btnFromPhoto.setOnClickListener {
            // Navigate to a fragment that handles photo-based recipe creation
            findNavController().navigate(R.id.action_recipeManagementFragment_to_photoRecipeFragment)
        }

        // Button: Create Recipe from Text
        binding.btnFromText.setOnClickListener {
            // Navigate to a fragment that handles text-based recipe creation
            findNavController().navigate(R.id.action_recipeManagementFragment_to_textRecipeFragment)
        }

        // Register gallery picker for selecting images for recipe creation
        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    // Pass the image URI to the fragment for handling
                    val action = RecipeManagementFragmentDirections
                        .actionRecipeManagementFragmentToPhotoRecipeFragment(imageUri.toString())
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onTextChosen() {
        // Navigate to text-based recipe creation fragment
        findNavController().navigate(R.id.action_recipeManagementFragment_to_textRecipeFragment)
    }

    override fun onPhotoChosen() {
        // Trigger the gallery picker to select an image
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks by clearing binding reference
    }
}
