package com.nikdi.recipefyai.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun arePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(context, it) }
    }

    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun showSettingsDialog(context: Context, activity: Activity) {
        AlertDialog.Builder(context)
            .setTitle("Необходими разрешения")
            .setMessage("Моля позволете на приложението необходимите разрешения!")
            .setPositiveButton("Отиди в Настройки") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", activity.packageName, null)
                }
                activity.startActivity(intent)
            }
            .setNegativeButton("Откажи", null)
            .show()
    }

    fun checkPermissions(context: Context, requiredPermissions: Array<String>): Boolean {
        return this.arePermissionsGranted(context, requiredPermissions)
    }

    fun onRequestPermissionsResult(requestCode: Int, activity: Activity, proceedToApp: () -> Unit, showSettingsDialog: () -> Unit) {
        if (requestCode == RequiredPermissions.PERMISSION_REQUEST_CODE) {
            if (arePermissionsGranted(activity, RequiredPermissions.permissions)) {
                proceedToApp() // Permissions granted, proceed to app
            } else {
                showSettingsDialog() // Permissions denied, show settings dialog
            }
        }
    }
}