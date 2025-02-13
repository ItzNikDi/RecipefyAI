package com.nikdi.recipefyai.recipes

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
    private var detectedIngredients = mutableListOf<String>()
    private lateinit var loadingOverlay: View
    private var selectedServings: String? = null
    private var selectedUnits: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngredientChoosingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        loadingOverlay = binding.loadingOverlay // Use binding to access the overlay
        val imageUriString = args.imageUri

        val imageUri = if (!imageUriString.isNullOrEmpty()) {
            Uri.parse(imageUriString)
        } else {
            null
        }

        if (imageUri != null) {
            showLoading(true)

            ingredientAdapter.notifyDataSetChanged()
            coroutineScope.launch(Dispatchers.IO) {
                var infTime = System.currentTimeMillis()
                initModel()
                runInference(imageUri)
                infTime = System.currentTimeMillis() - infTime
                Log.d("InfTime", infTime.toString())
            }
        } else {
            ingredientAdapter.notifyDataSetChanged() // No image, so just show an empty list
        }

        // Set up the add ingredient button click listener
        binding.addIngredientButton.setOnClickListener { view ->
            val newIngredient = binding.ingredientInput.text.toString().trim()
            if (newIngredient.isNotEmpty()) {
                ingredientAdapter.notifyDataSetChanged()
                if (detectedIngredients.contains(newIngredient)) {
                    Snackbar.make(view, getString(R.string.duplicate_ingredient), Snackbar.LENGTH_SHORT).show()
                } else {
                    detectedIngredients.add(newIngredient)
                    ingredientAdapter.notifyItemInserted(detectedIngredients.size - 1)
                    binding.ingredientInput.text.clear() // Clear input field after adding
                    updateEmptyOverlay(getString(R.string.no_added_ingredients))
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
            val dialog = RecipeDetailsDialog.newInstance(selectedServings, selectedUnits)
            dialog.show(childFragmentManager, "RecipeDetailsDialog")
        }
    }

    private fun updateEmptyOverlay(message: String) {
        if (detectedIngredients.isEmpty()) {
            binding.emptyMessage.text = message
            binding.emptyMessage.animate().alpha(1f).setDuration(100).withStartAction {
                binding.emptyMessage.visibility = View.VISIBLE
            }.start()// Fade in
        } else {
            binding.emptyMessage.animate().alpha(0f).setDuration(100).withEndAction {
                binding.emptyMessage.visibility = View.GONE
            }.start() // Fade out
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
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
                                yoloDetector?.detect(bitmap)
                            }
                        }
                    },
                    onError = {
                        Snackbar.make(binding.root, getString(R.string.inference_error), Snackbar.LENGTH_SHORT).show()
                    }
                )
                .build()

            Coil.imageLoader(requireContext()).enqueue(request)
        }
    }

    private fun setupRecyclerView() {
        ingredientAdapter = IngredientAdapter(detectedIngredients) { position -> ingredientAdapter.removeIngredient(position); this.updateEmptyOverlay(getString(R.string.no_added_ingredients)) }
        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = ingredientAdapter
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        coroutineScope.launch(Dispatchers.Main) {
            showLoading(false)
            detectedIngredients.addAll(boundingBoxes.map { it.clsName })
            ingredientAdapter.removeDuplicates() // Handle duplicates if necessary
            ingredientAdapter.notifyDataSetChanged()
            updateEmptyOverlay(getString(R.string.no_ingredients_found))
        }
    }

    override fun onEmptyDetect() {
        coroutineScope.launch(Dispatchers.Main) {
            showLoading(false)
            updateEmptyOverlay(getString(R.string.no_ingredients_found))
        }
    }

    override fun onDetailsEntered(servings: String, units: String) {
        selectedServings = servings
        selectedUnits = units

        val action = IngredientChoosingFragmentDirections
            .actionIngredientChoiceFragmentToTemporaryRecipeFragment(
                detectedIngredients.toTypedArray(),
                servings.toInt(),
                units)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        yoloDetector?.close()
        coroutineScope.cancel()
        selectedServings = null
        selectedUnits = null
    }
}
