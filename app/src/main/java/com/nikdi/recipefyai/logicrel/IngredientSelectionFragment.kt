package com.nikdi.recipefyai.logicrel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
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
import com.nikdi.recipefyai.MainActivity
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.airel.BoundingBox
import com.nikdi.recipefyai.airel.YOLODetector
import com.nikdi.recipefyai.databinding.FragmentIngredientSelectionBinding
import com.nikdi.recipefyai.utils.IngredientAdapter
import kotlinx.coroutines.*

class IngredientSelectionFragment : Fragment(), YOLODetector.DetectorListener, RecipeDetailsDialog.RecipeDetailsListener {
    private var _binding: FragmentIngredientSelectionBinding? = null
    private val binding get() = _binding!!
    private val args: IngredientSelectionFragmentArgs by navArgs()
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
        (activity as MainActivity).setUpActionBarForFragment(this)
        _binding = FragmentIngredientSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        loadInformation(requireContext())

        binding.root.post { updateEmptyOverlay() }

        loadingOverlay = binding.loadingOverlay

        val imageUriString = args.imageUri
        val imageUri = imageUriString?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }

        if (imageUri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                showLoading(true)
                initModel()
                runInference(imageUri)
            }
        }

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

        binding.removeAllButton.setOnClickListener {
            ingredientsList.clear()
            ingredientAdapter.notifyDataSetChanged()
            updateEmptyOverlay()
            selectedServings = null
            selectedPortionSize = null
        }
    }

    private fun updateEmptyOverlay(message: String = getString(R.string.no_added_ingredients)) {
        if (ingredientsList.isEmpty()) {
            if (binding.emptyMessage.visibility != View.VISIBLE) {
                binding.emptyMessage.text = message
                binding.emptyMessage.alpha = 0f
                binding.emptyMessage.visibility = View.VISIBLE
                binding.emptyMessage.animate().cancel()
                binding.emptyMessage.animate().alpha(1f).setDuration(150).start()
            }
        } else {
            if (binding.emptyMessage.visibility == View.VISIBLE) {
                binding.emptyMessage.animate().cancel()
                binding.emptyMessage.animate().alpha(0f).setDuration(150).withEndAction {
                    binding.emptyMessage.visibility = View.GONE
                }.start()
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

    private fun saveInformation(ingredients: List<String>, servings: String, portionSize: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("InformationPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putStringSet("ingredients", ingredients.toSet())
            .putString("servings", servings)
            .putString("portion_size", portionSize)
            .apply()
    }

    private fun loadInformation(context: Context) {
        val sharedPreferences = context.getSharedPreferences("InformationPrefs", Context.MODE_PRIVATE)
        ingredientsList.clear()
        ingredientsList.addAll(sharedPreferences.getStringSet("ingredients", emptySet())?.toList() ?: emptyList())
        ingredientAdapter.notifyDataSetChanged()
        selectedServings = sharedPreferences.getString("servings", null)
        selectedPortionSize = sharedPreferences.getString("portion_size", null)
    }

    private suspend fun initModel() {
        withContext(Dispatchers.IO) {
            yoloDetector = YOLODetector(
                context = requireContext(),
                modelPath = "internet.tflite",
                labelPath = "labels.txt",
                detectorListener = this@IngredientSelectionFragment,
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
                                        showLoading(false)
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        showLoading(false)
                                        Snackbar.make(binding.root, getString(R.string.inference_error), Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    onError = {
                        showLoading(false)
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
            ingredientAdapter.removeDuplicates()
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

        val action = IngredientSelectionFragmentDirections
            .actionIngredientSelectionFragmentToTemporaryRecipeFragment(
                ingredientsList.toTypedArray(),
                servings.toInt(),
                portionSize.toFloat())
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        saveInformation(
            ingredientsList,
            selectedServings ?: "",
            selectedPortionSize ?: "",
            requireContext()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        yoloDetector?.close()
        coroutineScope.cancel()
    }
}
