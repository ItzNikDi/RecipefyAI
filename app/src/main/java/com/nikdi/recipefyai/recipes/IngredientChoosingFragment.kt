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

class IngredientChoosingFragment : Fragment(), YOLODetector.DetectorListener {
    private var _binding: FragmentIngredientChoosingBinding? = null
    private val binding get() = _binding!!
    private val args: IngredientChoosingFragmentArgs by navArgs()
    private var yoloDetector: YOLODetector? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var ingredientAdapter: IngredientAdapter
    private var detectedIngredients = mutableListOf<String>()
    private lateinit var loadingOverlay: View

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

        updateEmptyOverlay()

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
                    updateEmptyOverlay()
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
    }

    private fun updateEmptyOverlay() {
        if (detectedIngredients.isEmpty()) {
            binding.emptyMessage.animate().alpha(1f).setDuration(100).withStartAction {
                binding.emptyMessage.visibility = View.VISIBLE
            }.start()// Fade in
        } else {
            binding.emptyMessage.animate().alpha(0f).setDuration(100).withStartAction() {
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
                modelPath = "internet.tflite",
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
        ingredientAdapter = IngredientAdapter(detectedIngredients) { position -> ingredientAdapter.removeIngredient(position); this.updateEmptyOverlay() }
        binding.ingredientRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ingredientRecyclerView.adapter = ingredientAdapter
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        coroutineScope.launch(Dispatchers.Main) {
            showLoading(false)
            detectedIngredients.addAll(boundingBoxes.map { it.clsName })
            ingredientAdapter.removeDuplicates() // Handle duplicates if necessary
            ingredientAdapter.notifyDataSetChanged()
        }
    }

    override fun onEmptyDetect() {
        coroutineScope.launch(Dispatchers.Main) {
            showLoading(false)
            updateEmptyOverlay()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        yoloDetector?.close()
        coroutineScope.cancel()
    }
}
