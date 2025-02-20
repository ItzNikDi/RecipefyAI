package com.nikdi.recipefyai.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nikdi.recipefyai.MainActivity
import com.nikdi.recipefyai.databinding.FragmentDisplaySavedRecipeBinding
import com.nikdi.recipefyai.dbrel.Recipe
import com.nikdi.recipefyai.dbrel.RecipeSQLiteHelper
import io.noties.markwon.Markwon

class DisplaySavedRecipeFragment: Fragment() {
    private var _binding: FragmentDisplaySavedRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: RecipeSQLiteHelper
    private lateinit var recipe: Recipe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (activity as MainActivity).setUpActionBarForFragment(this)
        _binding = FragmentDisplaySavedRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = arguments?.getString("recipe_id") ?: return

        database = RecipeSQLiteHelper(requireContext())

        recipe = database.getRecipeById(recipeId)

        (activity as MainActivity).supportActionBar?.title = recipe.name

        displayMarkdown(recipe.preparation)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = recipe.name
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