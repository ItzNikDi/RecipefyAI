package com.nikdi.recipefyai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nikdi.recipefyai.recipes.RecipeManagementActivity
import com.nikdi.recipefyai.utils.PermissionManager
import com.nikdi.recipefyai.utils.PreferenceManager
import com.nikdi.recipefyai.utils.RequiredPermissions

class MainActivity: AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        if (PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            proceedToApp()
        } else {
            if (preferenceManager.isFirstRun()) {
                PermissionManager.requestPermissions(this, RequiredPermissions.permissions, RequiredPermissions.PERMISSION_REQUEST_CODE)
            } else {
                PermissionManager.showSettingsDialog(this, this)
            }
        }
    }

    private fun proceedToApp() {
        preferenceManager.setFirstRun(false)
        Toast.makeText(this, "Добре дошли!", Toast.LENGTH_SHORT).show() // TO REMOVE

        startActivity(Intent(this, RecipeManagementActivity::class.java))
        finish()
    }
}