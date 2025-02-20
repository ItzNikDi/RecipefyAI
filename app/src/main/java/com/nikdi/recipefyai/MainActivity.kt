package com.nikdi.recipefyai

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.nikdi.recipefyai.utils.PermissionManager
import com.nikdi.recipefyai.utils.PreferenceManager
import com.nikdi.recipefyai.utils.RequiredPermissions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.nikdi.recipefyai.databinding.ActivityMainBinding
import com.nikdi.recipefyai.dbrel.RecipeSQLiteHelper
import com.nikdi.recipefyai.dbrel.RecipeSummary
import com.nikdi.recipefyai.recipes.DisplaySavedRecipeFragment
import com.nikdi.recipefyai.recipes.IngredientSelectionFragment
import com.nikdi.recipefyai.recipes.NewRecipeFragment
import com.nikdi.recipefyai.recipes.TemporaryRecipeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: RecipeSQLiteHelper
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var alreadyProceeded = false  // Prevents multiple calls

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            proceedToApp()
        } else {
            if (preferenceManager.isFirstRun()) {
                PermissionManager.requestPermissions(
                    this,
                    RequiredPermissions.permissions,
                    RequiredPermissions.PERMISSION_REQUEST_CODE
                )
            } else {
                PermissionManager.showSettingsDialog(this, this)
            }
        }

        database = RecipeSQLiteHelper(this)

        // DrawerLayout setup
        drawerLayout = binding.drawerLayout
        navView = binding.navigationView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_sidebar, R.string.close_sidebar
        )
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.newRecipeFragment, R.id.displaySavedRecipeFragment), drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        populateRecipesMenu(database.getAllRecipeNamesSorted())
        navView.invalidate()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_new_recipe -> navigateTo(R.id.newRecipeFragment)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        if (savedInstanceState == null) {
            navigateTo(R.id.newRecipeFragment)
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

    private fun populateRecipesMenu(recipes: List<RecipeSummary>) {
        val navView = findViewById<NavigationView>(R.id.navigation_view)
        val menu = navView.menu
        val recipesGroup = menu.findItem(R.id.nav_recipes_group)?.subMenu ?: return

        recipesGroup.clear()

        for (recipe in recipes) {
            Log.d("DBDebug", "ID: ${recipe.id}, Name: ${recipe.name}")
            recipesGroup.add(Menu.NONE, recipe.id.hashCode(), Menu.NONE, recipe.name)
                .setOnMenuItemClickListener {
                    openRecipe(recipe.id)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
        }
    }


    // Navigate to correct fragment
    private fun openRecipe(recipeId: String) {
        val bundle = Bundle().apply {
            putString("recipe_id", recipeId)
        }
        // Use the NavController to navigate using actions defined in the nav_graph
        findNavController(R.id.nav_host_fragment).navigate(R.id.displaySavedRecipeFragment, bundle)
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

    private fun navigateTo(destinationId: Int) {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        navController.navigate(destinationId)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer if open
        }
    }

    fun setUpActionBarForFragment(fragment: Fragment) {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        when (fragment) {
            is NewRecipeFragment, is DisplaySavedRecipeFragment -> {
                binding.toolbar.setNavigationOnClickListener {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            is TemporaryRecipeFragment, is IngredientSelectionFragment -> {
                binding.toolbar.setNavigationOnClickListener {
                    navController.popBackStack()
                }
            }
        }
    }

}
