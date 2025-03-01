package com.nikdi.recipefyai.logicrel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var enableButtonRunnable: Runnable
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemporaryRecipeBinding.inflate(inflater, container, false)

        recipeViewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableButtonRunnable = Runnable {
            if(isAdded) {
                binding.btnRegenRecipe.apply {
                    isEnabled = true
                    isClickable = true
                    alpha = 1f
                    text = getString(R.string.regenerate_recipe)
                }
            }
        }
        loadingOverlay = binding.loadingOverlay
        showLoading(true)

        val recipeRequest =
            RecipeRequest(args.ingredients.toList(), args.servings, args.portionSize)

        startCooldown()
        sendRecipeToServer(recipeRequest)

        (activity as MainActivity).setUpActionBarForFragment(this)
        (activity as MainActivity).setUpActionBarForTemporaryRecipes(false)

        binding.btnSaveRecipe.setOnClickListener {
            if (isRecipeValid) {
                showNameDialog()
            } else {
                Snackbar.make(view, getString(R.string.invalid_recipe), Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.buttonLayout)
                    .show()
            }
        }

        binding.btnRegenRecipe.setOnClickListener {
            startCooldown()
            sendRecipeToServer(recipeRequest)
        }

        binding.vertScrollView.isSmoothScrollingEnabled = true
    }

    private fun sendRecipeToServer(recipeRequest: RecipeRequest) {
        showLoading(true)

        binding.btnRegenRecipe.apply {
            isEnabled = false
            isClickable = false
            alpha = 0.5f
        }
        binding.btnSaveRecipe.apply {
            isEnabled = false
            isClickable = false
            alpha = 0.5f
        }

        (activity as MainActivity).setUpActionBarForTemporaryRecipes(false)

        RetrofitClient.apiService.generateRecipe(recipeRequest)
            .enqueue(object : Callback<RecipeResponse> {
                override fun onResponse(
                    call: Call<RecipeResponse>,
                    response: Response<RecipeResponse>
                ) {
                    if (!isAdded) return
                    showLoading(false)

                    if (response.isSuccessful) {
                        response.body()?.let { recipeResponse ->
                            val markdownResponse = recipeResponse.markdown
                            val extractedName = extractName(markdownResponse)
                            recipeName = extractedName
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
                            binding.btnSaveRecipe.apply {
                                isEnabled = true
                                isClickable = true
                                alpha = 1f
                            }
                        }
                    } else {
                        if (isAdded) binding.textViewOutput.text = getString(R.string.network_error)
                        isRecipeValid = false
                    }

                    (activity as MainActivity).setUpActionBarForTemporaryRecipes(true)
                }

                override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                    if (!isAdded) return
                    showLoading(false)
                    binding.textViewOutput.text = getString(R.string.network_error)
                    isRecipeValid = false
                    binding.btnSaveRecipe.apply {
                        isEnabled = false
                        isClickable = false
                        alpha = 0.5f
                    }
                    (activity as MainActivity).setUpActionBarForTemporaryRecipes(true)
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
        val regex = Regex("^#\\s?(.*)", RegexOption.MULTILINE)
        val match = regex.find(markdownText)
        return match?.groupValues?.get(1)
            ?.trim()?.removeSuffix(":")
            ?: getString(R.string.unnamed_recipe)
    }

    private fun startCooldown() {
        binding.btnRegenRecipe.apply {
            isEnabled = false
            isClickable = false
            alpha = 0.5f
            text = getString(R.string.please_wait_regen_block)
        }

        handler.postDelayed(enableButtonRunnable, 25_000)
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
        handler.removeCallbacks(enableButtonRunnable)
    }
}