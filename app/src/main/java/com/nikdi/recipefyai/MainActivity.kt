package com.nikdi.recipefyai

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.nikdi.recipefyai.utils.PermissionManager
import com.nikdi.recipefyai.utils.PreferenceManager
import com.nikdi.recipefyai.utils.RequiredPermissions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nikdi.recipefyai.databinding.ActivityMainBinding
import com.nikdi.recipefyai.utils.RecipeAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var recipeAdapter: RecipeAdapter
    private var alreadyProceeded = false  // Prevents multiple calls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // DrawerLayout setup
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            val navController = it.navController
            appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        } ?: Log.e("MainActivity", "NavHostFragment is null!")

        val navigationView: RecyclerView = findViewById(R.id.nav_view)
        navigationView.layoutManager = LinearLayoutManager(this)

        val recipeList = mutableListOf("New Recipe") // Ensure at least one item is there
        recipeAdapter = RecipeAdapter(recipeList) { recipeName ->
            if (recipeName == "New Recipe") {
                findNavController(R.id.nav_host_fragment).navigate(R.id.newRecipeFragment)
            } else {
                Toast.makeText(this, "Clicked on $recipeName", Toast.LENGTH_SHORT).show()
            }
        }

        navigationView.adapter = recipeAdapter  // ✅ FIXED

        if (PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            proceedToApp()
        } else {
            if (preferenceManager.isFirstRun()) {
                PermissionManager.requestPermissions(
                    this, RequiredPermissions.permissions, RequiredPermissions.PERMISSION_REQUEST_CODE
                )
            } else {
                PermissionManager.showSettingsDialog(this, this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // If permissions are granted, proceed
        if (PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            if (!alreadyProceeded) {  // Ensure it runs only once
                alreadyProceeded = true
                proceedToApp()
            }
        } else {
            // If permissions are not granted, show the settings dialog
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
            ::proceedToApp  // Proceed when permissions are granted
        ) {
            PermissionManager.showSettingsDialog(this, this)  // Show settings dialog if permissions are denied
        }
    }

    private fun proceedToApp() {
        if (preferenceManager.isFirstRun()) preferenceManager.setFirstRun(false)
        // Navigate to the "New Recipe" screen (adjust the ID as per your setup)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.newRecipeFragment)
        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
        return NavigationUI.navigateUp(navController!!, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
