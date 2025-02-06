package com.nikdi.recipefyai.recipes

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.Coil
import coil.request.ImageRequest
import com.nikdi.recipefyai.R
import com.nikdi.recipefyai.airel.BoundingBox
import com.nikdi.recipefyai.airel.YOLODetector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecipeCreationActivity : AppCompatActivity(), YOLODetector.DetectorListener {
    private lateinit var yoloDetector: YOLODetector
    private lateinit var executorService: ExecutorService
    private var detectedIngredients = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_creation)

        val imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }
        imageUri?.let { runInference(it) }

        yoloDetector = YOLODetector(
            context = this,
            modelPath = "food_float16.tflite",
            labelPath = "labels.txt",
            detectorListener = this,
            message = { msg -> runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() } }
        )

        executorService = Executors.newSingleThreadExecutor()
    }

    private fun runInference(uri: Uri) {
        val request = ImageRequest.Builder(this)
            .data(uri)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .target(
                onSuccess = { result ->
                    (result as? BitmapDrawable)?.bitmap?.let { bitmap ->
                        executorService.execute { yoloDetector.detect(bitmap) }
                    }
                },
                onError = {
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
                }
            )
            .build()

        Coil.imageLoader(this).enqueue(request)
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            detectedIngredients.clear()
            detectedIngredients.addAll(boundingBoxes.map { it.clsName })
            Toast.makeText(this, "Detected: ${detectedIngredients.joinToString(", ")}", Toast.LENGTH_LONG).show()

            // TODO: Pre-fill ingredient list UI with detectedIngredients
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            Toast.makeText(this, "No ingredients detected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        yoloDetector.close()
        executorService.shutdown()
    }
}

