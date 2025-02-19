package com.nikdi.recipefyai.recipes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.Coil
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.airel.BoundingBox
import com.nikdi.recipefyai.airel.YOLODetector
import com.nikdi.recipefyai.databinding.FragmentIngredientChoosingBinding
import com.nikdi.recipefyai.utils.IngredientAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

class IngredientChoosingFragment : Fragment(), YOLODetector.DetectorListener, RecipeDetailsDialog.RecipeDetailsListener {
    private var _binding: FragmentIngredientChoosingBinding? = null
    private val binding get() = _binding!!
    private val args: IngredientChoosingFragmentArgs by navArgs()
    private var yoloDetector: YOLODetector? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var ingredientAdapter: IngredientAdapter
    private var ingredientsList = mutableListOf<String>()
    private lateinit var loadingOverlay: View
    private var selectedServings: String? = null
    private var selectedPortionSize: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngredientChoosingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        ingredientsList.clear()
        ingredientsList.addAll(loadIngredients(requireContext()))

        ingredientAdapter.notifyDataSetChanged()

        binding.root.post { updateEmptyOverlay() }

        loadingOverlay = binding.loadingOverlay // Use binding to access the overlay

        val imageUriString = args.imageUri
        val imageUri = imageUriString?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }

        if (imageUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                showLoading(true)
                initModel()
                runInference(imageUri)
            }
        }

        // Set up the add ingredient button click listener
        binding.addIngredientButton.setOnClickListener {
            val newIngredients = binding.ingredientInput.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (newIngredients.isNotEmpty()) {
                var added = false
                for (ingredient in newIngredients) {
                    if (!ingredientsList.contains(ingredient)) {
                        ingredientsList.add(ingredient)
                        added = true
                    }
                }

                if (added) {
                    binding.ingredientInput.text.clear()
                    ingredientAdapter.notifyDataSetChanged()
                    updateEmptyOverlay()
                } else {
                    Snackbar.make(view, getString(R.string.duplicate_ingredient), Snackbar.LENGTH_SHORT)
                        .setAnchorView(R.id.addIngredientButton).show()
                }
            } else {
                Snackbar.make(view, getString(R.string.empty_ingredient_text), Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.addIngredientButton).show()
            }
        }

        // Handle Enter key press in the EditText
        binding.ingredientInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.addIngredientButton.performClick()
                true
            } else {
                false
            }
        }

        binding.confirmIngredientsButton.setOnClickListener {
            if (ingredientsList.isNotEmpty()) {
                showDetailsDialog()
            } else {
                Snackbar.make(view, getString(R.string.no_added_ingredients), Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.confirmIngredientsButton).show()
            }
        }
    }

    private fun updateEmptyOverlay(message: String = getString(R.string.no_added_ingredients)) {
        if (ingredientsList.isEmpty()) {
            if (binding.emptyMessage.visibility != View.VISIBLE) {
                binding.emptyMessage.text = message
                binding.emptyMessage.alpha = 0f
                binding.emptyMessage.visibility = View.VISIBLE
                binding.emptyMessage.animate().cancel() // Cancel ongoing animations
                binding.emptyMessage.animate().alpha(1f).setDuration(150).start() // Fade in
            }
        } else {
            if (binding.emptyMessage.visibility == View.VISIBLE) {
                binding.emptyMessage.animate().cancel()
                binding.emptyMessage.animate().alpha(0f).setDuration(150).withEndAction {
                    binding.emptyMessage.visibility = View.GONE
                }.start() // Fade out
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showDetailsDialog() {
        val dialog = RecipeDetailsDialog.newInstance(selectedServings, selectedPortionSize)
        dialog.show(childFragmentManager, "RecipeDetailsDialog")
    }

    private fun saveIngredients(ingredients: List<String>, context: Context) {
        val sharedPreferences = context.getSharedPreferences("IngredientPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putStringSet("ingredients", ingredients.toSet())
            .apply()
    }

    private fun loadIngredients(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences("IngredientPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("ingredients", emptySet())?.toList() ?: emptyList()
    }

    private suspend fun initModel() {
        withContext(Dispatchers.IO) {
            yoloDetector = YOLODetector(
                context = requireContext(),
                modelPath = "internet.tflite", // TODO introduce the model
                labelPath = "labels.txt",
                detectorListener = this@IngredientChoosingFragment,
                message = { msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    private suspend fun runInference(uri: Uri) {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(requireContext())
                .data(uri)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .target(
                    onSuccess = { result ->
                        (result as? BitmapDrawable)?.bitmap?.let { bitmap ->
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    yoloDetector?.detect(bitmap)
                                    withContext(Dispatchers.Main) {
                                        showLoading(false) // Hide loading after inference is done
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        showLoading(false) // Ensure it hides even if an error occurs
                                        Snackbar.make(binding.root, getString(R.string.inference_error), Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    onError = {
                        showLoading(false) // Hide loading if an error happens
                        Snackbar.make(binding.root, getString(R.string.inference_error), Snackbar.LENGTH_SHORT).show()
                    }
                )
                .build()

            Coil.imageLoader(requireContext()).enqueue(request)
        }
    }


    private fun setupRecyclerView() {
        ingredientAdapter = IngredientAdapter(ingredientsList) { position -> ingredientAdapter.removeIngredient(position); updateEmptyOverlay() }
        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = ingredientAdapter
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        coroutineScope.launch(Dispatchers.Main) {
            ingredientsList.addAll(boundingBoxes.map { it.clsName })
            ingredientAdapter.removeDuplicates() // Handle duplicates if necessary
            ingredientAdapter.notifyDataSetChanged()
            updateEmptyOverlay()
        }
    }

    override fun onEmptyDetect() {
        coroutineScope.launch(Dispatchers.Main) {
            updateEmptyOverlay(getString(R.string.no_ingredients_found))
        }
    }

    override fun onCancel(servings: String, portionSize: String) {
        selectedServings = servings
        selectedPortionSize = portionSize
    }

    override fun onProceed(servings: String, portionSize: String) {
        selectedServings = servings
        selectedPortionSize = portionSize

        val action = IngredientChoosingFragmentDirections
            .actionIngredientChoiceFragmentToTemporaryRecipeFragment(
                ingredientsList.toTypedArray(),
                servings.toInt(),
                portionSize.toFloat())
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        saveIngredients(ingredientsList, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        yoloDetector?.close()
        coroutineScope.cancel()
    }
}
