package com.nikdi.recipefyai

import android.content.Intent
import android.os.Bundle
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

    override fun onResume() {
        super.onResume()
        if (PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            proceedToApp()
        } else {
            if (!preferenceManager.isFirstRun()) {
                PermissionManager.showSettingsDialog(this, this)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionManager.onRequestPermissionsResult(
            requestCode,
            this,
            ::proceedToApp
        )  // Permissions granted, proceed to app
        {
            PermissionManager.showSettingsDialog(this,this)
        }  // Show settings dialog if permissions denied

    }


    private fun proceedToApp() {
        if (preferenceManager.isFirstRun()) preferenceManager.setFirstRun(false)
        startActivity(Intent(this, RecipeManagementActivity::class.java))
        finish()
    }
}