package com.nikdi.recipefyai.recipes

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
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

            coroutineScope.launch(Dispatchers.Default) {
                var infTime = System.currentTimeMillis()
                initModel()
                runInference(imageUri)
                infTime = System.currentTimeMillis() - infTime
                Log.d("InfTime", infTime.toString())
            }
        } else {
            // If using text input, initialize an empty list (no detector needed)
            detectedIngredients.clear()
            ingredientAdapter.notifyDataSetChanged()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        runOnUiThread {
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private suspend fun initModel() {
        withContext(Dispatchers.Default) { // 🚀 Run on background thread
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
        withContext(Dispatchers.Default) {
            val request = ImageRequest.Builder(this@RecipeCreationActivity)
                .data(uri)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .target(
                    onSuccess = { result ->
                        (result as? BitmapDrawable)?.bitmap?.let { bitmap ->
                            coroutineScope.launch(Dispatchers.Default) {
                                yoloDetector?.detect(bitmap)
                            }
                        }
                    },
                    onError = {
                        coroutineScope.launch(Dispatchers.Main) {
                            detectedIngredients.clear()
                            detectedIngredients.add("Sorry, could not process image.")
                            ingredientAdapter.notifyDataSetChanged()
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
            detectedIngredients.clear()
            showLoading(false)
            detectedIngredients.addAll(boundingBoxes.map { it.clsName })
            ingredientAdapter.notifyDataSetChanged()
        }
    }

    override fun onEmptyDetect() {
        coroutineScope.launch(Dispatchers.Main){
            detectedIngredients.clear()
            showLoading(false)
            detectedIngredients.add("No products detected, please add manually!")
            ingredientAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        yoloDetector?.close()
        coroutineScope.cancel()
    }
}

