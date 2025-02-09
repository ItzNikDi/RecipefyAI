package com.nikdi.recipefyai.recipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.nikdi.recipefyai.R


class RecipeManagementActivity : AppCompatActivity(), RecipeMethodDialog.RecipeMethodListener {
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_management)

        findViewById<Button>(R.id.btn_new_recipe).setOnClickListener {
            RecipeMethodDialog().show(supportFragmentManager, "choose_method")
        }

        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    val intent = Intent(this, RecipeCreationActivity::class.java)
                    intent.putExtra("image_uri", imageUri.toString()) // Pass image to creation activity
                    startActivity(intent)
                }
            }
        }
    }

    override fun onTextChosen() {
        val intent = Intent(this, RecipeCreationActivity::class.java)
        startActivity(intent)
    }

    override fun onPhotoChosen() {
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }
}
