package com.nikdi.recipefyai.recipes

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
import com.nikdi.recipefyai.databinding.FragmentNewRecipeBinding
import com.nikdi.recipefyai.R

class NewRecipeFragment : Fragment() {
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

        // Button: Create Recipe from Photo
        binding.btnFromPhoto.setOnClickListener {
            // Navigate to a fragment that handles photo-based recipe creation
            openGallery()
        }

        // Button: Create Recipe from Text
        binding.btnFromText.setOnClickListener {
            // Navigate to a fragment that handles text-based recipe creation
            val action = NewRecipeFragmentDirections.actionNewRecipeFragmentToRecipeCreationFragment(null)
            findNavController().navigate(action)
        }

        // Register gallery picker for selecting images for recipe creation
        galleryResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    val action = NewRecipeFragmentDirections.actionNewRecipeFragmentToRecipeCreationFragment(imageUri.toString())
                    findNavController().navigate(action)
                }
            }
        }
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
