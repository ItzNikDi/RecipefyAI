package com.nikdi.recipefyai.recipes

import android.content.Context
import android.content.SharedPreferences
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
    //private val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("AppState", Context.MODE_PRIVATE) //TODO finish up the last point of entry logic
    private var _binding: FragmentDisplaySavedRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recipeId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySavedRecipeBinding.inflate(inflater, container, false)

        recipeId = arguments?.getString("recipe_id") ?: return binding.root

        (activity as MainActivity).setUpActionBarForSavedRecipes(recipeId)

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        recipeViewModel.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            recipe?.let {
                displayMarkdown(recipe.preparation)
                binding.vertScrollView.isSmoothScrollingEnabled = true
                binding.textViewOutput.setHorizontallyScrolling(true)
                (activity as MainActivity).supportActionBar?.title = recipe.name
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vertScrollView.isSmoothScrollingEnabled = true
        binding.textViewOutput.setHorizontallyScrolling(true)
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