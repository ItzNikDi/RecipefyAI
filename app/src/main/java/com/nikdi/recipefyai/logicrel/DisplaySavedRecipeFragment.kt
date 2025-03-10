package com.nikdi.recipefyai.logicrel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nikdi.recipefyai.MainActivity
import com.nikdi.recipefyai.databinding.FragmentDisplaySavedRecipeBinding
import com.nikdi.recipefyai.viewmodel.RecipeViewModel
import io.noties.markwon.Markwon

class DisplaySavedRecipeFragment: Fragment() {
    private var _binding: FragmentDisplaySavedRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recipeId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySavedRecipeBinding.inflate(inflater, container, false)

        recipeId = arguments?.getString("recipe_id") ?: return binding.root

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        recipeViewModel.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                displayMarkdown(recipe.preparation)
                binding.vertScrollView.isSmoothScrollingEnabled = true
                (activity as MainActivity).supportActionBar?.title = recipe.name
            }
        }

        (activity as MainActivity).setUpActionBarForSavedRecipes(recipeId)

        return binding.root
    }

    private fun displayMarkdown(markdownText: String) {
        val markdownView = Markwon.create(requireContext())
        markdownView.setMarkdown(binding.textViewOutput, markdownText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}