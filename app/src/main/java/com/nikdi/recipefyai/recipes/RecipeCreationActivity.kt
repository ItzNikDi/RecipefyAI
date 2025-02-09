package com.nikdi.recipefyai.recipes

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.airel.BoundingBox
import com.nikdi.recipefyai.airel.YOLODetector
import com.nikdi.recipefyai.utils.IngredientAdapter
import kotlinx.coroutines.*

class RecipeCreationActivity : AppCompatActivity(), YOLODetector.DetectorListener {
    private var yoloDetector: YOLODetector? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var ingredientAdapter: IngredientAdapter
    private var detectedIngredients = mutableListOf<String>()
    private lateinit var loadingOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_creation)

        setupRecyclerView()
        loadingOverlay = findViewById(R.id.loadingOverlay)
        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }
        
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
            // If using text input, initialize an empty list (no detector needed)
            ingredientAdapter.notifyDataSetChanged()
        }

        val ingredientInput = findViewById<EditText>(R.id.ingredientInput)
        val addIngredientButton = findViewById<Button>(R.id.addIngredientButton)

        addIngredientButton.setOnClickListener { view ->
            val newIngredient = ingredientInput.text.toString().trim()

            if (newIngredient.isNotEmpty()) {
                ingredientAdapter.notifyDataSetChanged()
                if(detectedIngredients.contains(newIngredient)) { Snackbar.make(view, getString(R.string.duplicate_ingredient), Snackbar.LENGTH_SHORT).show()}
                else {
                    detectedIngredients.add(newIngredient)
                    ingredientAdapter.notifyItemInserted(detectedIngredients.size - 1)
                    ingredientInput.text.clear()
                }
            } else {
                Snackbar.make(view, getString(R.string.empty_ingredient_text), Snackbar.LENGTH_SHORT).setAnchorView(R.id.addIngredientButton).show()
            }
        }
        ingredientInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addIngredientButton.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private suspend fun initModel() {
        withContext(Dispatchers.IO) { // 🚀 Run on background thread
            yoloDetector = YOLODetector(
                context = this@RecipeCreationActivity,
                modelPath = "internet.tflite",
                labelPath = "labels.txt",
                detectorListener = this@RecipeCreationActivity,
                message = { msg -> runOnUiThread { Toast.makeText(this@RecipeCreationActivity, msg, Toast.LENGTH_SHORT).show() } }
            )
        }
    }

    private suspend fun runInference(uri: Uri) {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(this@RecipeCreationActivity)
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
                        runOnUiThread {
                            Snackbar.make(findViewById(R.id.linear_creation_layout), getString(R.string.inference_error), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                )
                .build()

            Coil.imageLoader(this@RecipeCreationActivity).enqueue(request)
        }
    }

    private fun setupRecyclerView() {
        ingredientAdapter = IngredientAdapter(detectedIngredients) { ingredient ->
            removeIngredient(ingredient)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.ingredientRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ingredientAdapter
    }

    private fun removeIngredient(ingredient: String) {
        detectedIngredients.remove(ingredient)
        ingredientAdapter.notifyDataSetChanged()
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>) {
        coroutineScope.launch(Dispatchers.Main) {
            showLoading(false)
            detectedIngredients.addAll(boundingBoxes.map { it.clsName })
            ingredientAdapter.removeDuplicates()
            ingredientAdapter.notifyDataSetChanged()
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            showLoading(false)
            Snackbar.make(findViewById(R.id.linear_creation_layout), getString(R.string.no_ingredients_found), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        yoloDetector?.close()
        coroutineScope.cancel()
    }
}

