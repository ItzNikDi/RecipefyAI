package com.nikdi.recipefyai.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.nikdi.recipefyai.databinding.FragmentTemporaryRecipeBinding
import org.json.JSONArray
import org.json.JSONObject

class TemporaryRecipeFragment : Fragment() {
    private var _binding: FragmentTemporaryRecipeBinding? = null
    private val binding get() = _binding!!
    val args: TemporaryRecipeFragmentArgs by navArgs()
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

        val recipeJson = createRecipeJson(args.ingredients.toList(), args.servings, args.units)

        sendRecipeToServer(recipeJson)

        binding.btnSaveRecipe.setOnClickListener { saveRecipe(recipeJson) }
        binding.btnRecreateRecipe.setOnClickListener { sendRecipeToServer(recipeJson) }
    }

    private fun createRecipeJson(ingredients: List<String>, servings: Int, units: String): String {
        val jsonObject = JSONObject().apply {
            put("ingredients", JSONArray(ingredients))
            put("servings", servings)
            put("units", units)
        }
        return jsonObject.toString()
    }

    private fun sendRecipeToServer(jsonData: String) {
        binding.textViewOutput.text = "Generating your recipe..." // Placeholder text

        // Use Volley or Retrofit to send request
        val url = "http://your-flask-server.com/generate_recipe"
        val request = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(jsonData),
            { response ->
                // Handle server response
                val markdownText = response.getString("recipe_text")
                displayMarkdown(markdownText)
            },
            { error ->
                binding.textViewOutput.text = "Error fetching recipe."
                Log.e("API_ERROR", "Error: ${error.message}")
            }
        )

        // Add request to queue
        Volley.newRequestQueue(requireContext()).add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}