package com.nikdi.recipefyai.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.apirel.RecipeRequest
import com.nikdi.recipefyai.apirel.RecipeResponse
import com.nikdi.recipefyai.apirel.RetrofitClient
import com.nikdi.recipefyai.databinding.FragmentTemporaryRecipeBinding
import io.noties.markwon.Markwon
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TemporaryRecipeFragment : Fragment() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var _binding: FragmentTemporaryRecipeBinding? = null
    private val binding get() = _binding!!
    private val args: TemporaryRecipeFragmentArgs by navArgs()
    private lateinit var loadingOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemporaryRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingOverlay = binding.loadingOverlay
        Log.d("Loaded", args.ingredients.toList().toString())
        Log.d("Loaded", args.servings.toString())
        Log.d("Loaded", args.servings.toString())
        showLoading(true)
        val recipeRequest = RecipeRequest(args.ingredients.toList(), args.servings, args.servings.toString())

        sendRecipeToServer(recipeRequest)

        binding.btnSaveRecipe.setOnClickListener { saveRecipe(recipeRequest) }
        binding.btnRegenRecipe.setOnClickListener { sendRecipeToServer(recipeRequest) }

        binding.vertScrollView.isSmoothScrollingEnabled = true
        binding.textViewOutput.setHorizontallyScrolling(true)
    }

    private fun sendRecipeToServer(recipeRequest: RecipeRequest) {
        showLoading(true)  // Show loading overlay
        binding.btnRegenRecipe.isEnabled = false  // Disable button to prevent spam requests

        RetrofitClient.apiService.generateRecipe(recipeRequest).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                showLoading(false)  // Hide loading
                binding.btnRegenRecipe.isEnabled = true  // Re-enable button

                if (response.isSuccessful && response.body() != null) {
                    val markdownResponse = response.body()!!.markdown
                    val userId = response.body()!!.uuid
                    Log.d("RecipeResponse", markdownResponse)
                    Log.d("RecipeResponse", userId)

                    response.body()?.let {
                        displayMarkdown(it.markdown)
                    }
                } else {
                    binding.textViewOutput.text = String.format("${getString(R.string.network_error)} ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                showLoading(false)  // Hide loading
                binding.btnRegenRecipe.isEnabled = true  // Re-enable button
                Log.e("RecipeError", "${t.message}")
                binding.textViewOutput.text = getString(R.string.network_error)
            }
        })
    }


    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun displayMarkdown(markdownText: String) {
        val markdownView = Markwon.create(requireContext())
        markdownView.setMarkdown(binding.textViewOutput, markdownText)
    }

    private fun saveRecipe(recipeRequest: RecipeRequest) {
        val json = Gson().toJson(recipeRequest)
        Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}