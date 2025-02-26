package com.nikdi.recipefyai.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.MainActivity
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.apirel.RecipeRequest
import com.nikdi.recipefyai.apirel.RecipeResponse
import com.nikdi.recipefyai.apirel.RetrofitClient
import com.nikdi.recipefyai.databinding.FragmentTemporaryRecipeBinding
import com.nikdi.recipefyai.dbrel.Recipe
import com.nikdi.recipefyai.viewmodel.RecipeViewModel
import io.noties.markwon.Markwon
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

class TemporaryRecipeFragment : Fragment(), RecipeNameDialog.RecipeNameListener {
    private var _binding: FragmentTemporaryRecipeBinding? = null
    private val binding get() = _binding!!
    private val args: TemporaryRecipeFragmentArgs by navArgs()
    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var loadingOverlay: View
    private var currentRecipe: Recipe? = null
    private var isRecipeValid = false
    private var recipeName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemporaryRecipeBinding.inflate(inflater, container, false)

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = binding.loadingOverlay
        showLoading(true)

        val recipeRequest =
            RecipeRequest(args.ingredients.toList(), args.servings, args.portionSize)

        sendRecipeToServer(recipeRequest)

        (activity as MainActivity).setUpActionBarForFragment(this)

        binding.btnSaveRecipe.setOnClickListener {
            if (isRecipeValid) {
                showNameDialog()
            } else {
                Snackbar.make(view, getString(R.string.invalid_recipe), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.buttonLayout)
                    .show()
            }
        }
        binding.btnRegenRecipe.setOnClickListener { sendRecipeToServer(recipeRequest) }

        binding.vertScrollView.isSmoothScrollingEnabled = true
        binding.textViewOutput.setHorizontallyScrolling(true)
    }

    private fun sendRecipeToServer(recipeRequest: RecipeRequest) {
        showLoading(true)  // Show loading overlay
        binding.btnRegenRecipe.isEnabled = false  // Disable button to prevent spam requests
        binding.btnSaveRecipe.isEnabled = false

        RetrofitClient.apiService.generateRecipe(recipeRequest)
            .enqueue(object : Callback<RecipeResponse> {
                override fun onResponse(
                    call: Call<RecipeResponse>,
                    response: Response<RecipeResponse>
                ) {
                    showLoading(false)
                    binding.btnRegenRecipe.isEnabled = true

                    if (response.isSuccessful) {
                        response.body()?.let { recipeResponse ->
                            val markdownResponse = recipeResponse.markdown
                            val extractedName = extractName(markdownResponse)

                            currentRecipe = Recipe(
                                id = UUID.randomUUID().toString(),
                                name = extractedName,
                                ingredients = args.ingredients.toList(),
                                servings = args.servings,
                                portionSize = args.portionSize,
                                preparation = markdownResponse,
                                createdAt = System.currentTimeMillis(),
                                editedAt = System.currentTimeMillis()
                            )

                            displayMarkdown(markdownResponse)
                            isRecipeValid = true
                            binding.btnSaveRecipe.isEnabled = true
                        }
                    } else {
                        binding.textViewOutput.text = getString(R.string.network_error)
                        isRecipeValid = false
                        binding.btnSaveRecipe.isEnabled = false
                    }
                }

                override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                    showLoading(false)  // Hide loading
                    binding.textViewOutput.text = getString(R.string.network_error)
                    isRecipeValid = false  // Prevent saving
                    binding.btnRegenRecipe.isEnabled = true  // Re-enable button
                    binding.btnSaveRecipe.isEnabled = false
                }
            })
    }


    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showNameDialog() {
        val dialog = RecipeNameDialog.newInstance(recipeName)
        dialog.show(childFragmentManager, "RecipeNameDialog")
    }

    private fun extractName(markdownText: String): String {
        val regex = Regex("^#\\s(.+)", RegexOption.MULTILINE)
        val matchResult = regex.find(markdownText)
        return matchResult?.groupValues?.get(1)?.trim() ?: getString(R.string.unnamed_recipe)
    }

    private fun displayMarkdown(markdownText: String) {
        val markdownView = Markwon.create(requireContext())
        markdownView.setMarkdown(binding.textViewOutput, markdownText)
    }

    override fun onCancel(name: String) {
        recipeName = name
    }

    override fun onSaveRecipe(name: String) {
        recipeName = name
        currentRecipe?.let {
            val updatedRecipe = it.copy(name = name)
            recipeViewModel.saveRecipe(updatedRecipe)
            Snackbar.make(requireView(), getString(R.string.recipe_saved), Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.btnSaveRecipe)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}