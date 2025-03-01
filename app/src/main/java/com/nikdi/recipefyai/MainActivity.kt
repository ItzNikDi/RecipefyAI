package com.nikdi.recipefyai

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import com.nikdi.recipefyai.utils.PermissionManager
import com.nikdi.recipefyai.utils.PreferenceManager
import com.nikdi.recipefyai.utils.RequiredPermissions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.nikdi.recipefyai.databinding.ActivityMainBinding
import com.nikdi.recipefyai.dbrel.RecipeSummary
import com.nikdi.recipefyai.logicrel.IngredientSelectionFragment
import com.nikdi.recipefyai.logicrel.NewRecipeFragment
import com.nikdi.recipefyai.logicrel.TemporaryRecipeFragment
import com.nikdi.recipefyai.utils.CustomTypefaceSpan
import com.nikdi.recipefyai.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: RecipeViewModel
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var alreadyProceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        for (i in 0 until toolbar.childCount) {
            val view = toolbar.getChildAt(i)
            if (view is TextView) {
                view.ellipsize = TextUtils.TruncateAt.MARQUEE
                view.marqueeRepeatLimit = -1
                view.isSingleLine = true
                view.isSelected = true
                break
            }
        }

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

        viewModel = ViewModelProvider(this)[RecipeViewModel::class.java]

        setupDrawer()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(setOf(R.id.newRecipeFragment, R.id.displaySavedRecipeFragment), drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        viewModel.recipesLiveData.observe(this) { recipes ->
            populateRecipesMenu(recipes)
        }

        navigateToLastFragment()
    }

    override fun onResume() {
        super.onResume()
        if (!alreadyProceeded && PermissionManager.checkPermissions(this, RequiredPermissions.permissions)) {
            alreadyProceeded = true
            proceedToApp()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.onRequestPermissionsResult(requestCode, this, ::proceedToApp) {
            PermissionManager.showSettingsDialog(this, this)
        }
    }

    private fun setupDrawer() {
        drawerLayout = binding.drawerLayout
        navView = binding.navigationView

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.toolbar,
            R.string.open_sidebar, R.string.close_sidebar
        )

        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        applyFontToMenu(navView.menu)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_new_recipe -> {
                    navigateTo(R.id.newRecipeFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> true
            }
        }
    }

    //TODO put README and LICENSE in git

    private fun applyFontToMenu(menu: Menu) {
        val typeface = ResourcesCompat.getFont(this, R.font.comfortaa_variable)

        fun applyFontToItems(menu: Menu) {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                val spannableString = SpannableString(menuItem.title)
                spannableString.setSpan(CustomTypefaceSpan("", typeface!!), 0, spannableString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                menuItem.title = spannableString

                if (menuItem.hasSubMenu()) {
                    applyFontToItems(menuItem.subMenu!!)
                }
            }
        }

        applyFontToItems(menu)
    }

    private fun populateRecipesMenu(recipes: List<RecipeSummary>) {
        val menu = navView.menu
        val recipesGroup = menu.findItem(R.id.nav_recipes_group)?.subMenu ?: return

        recipesGroup.clear()

        val typeface = ResourcesCompat.getFont(this, R.font.comfortaa_variable)

        for (recipe in recipes) {
            val menuItem = recipesGroup.add(Menu.NONE, recipe.id.hashCode(), Menu.NONE, recipe.name)
            val spannableString = SpannableString(menuItem.title)
            spannableString.setSpan(CustomTypefaceSpan("", typeface!!), 0, spannableString.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            menuItem.title = spannableString
            menuItem.setOnMenuItemClickListener {
                openRecipe(recipe.id)
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun deleteRecipe(recipeId: String) {
        viewModel.viewModelScope.launch {
            viewModel.deleteRecipeById(recipeId)
        }
        navigateTo(R.id.newRecipeFragment)
    }

    private fun openRecipe(recipeId: String) {
        val bundle = Bundle().apply {
            putString("recipe_id", recipeId)
        }
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController.navigate(R.id.displaySavedRecipeFragment, bundle)
        saveLastFragment(R.id.displaySavedRecipeFragment, recipeId)
    }

    private fun proceedToApp() {
        if (alreadyProceeded) return
        alreadyProceeded = true

        if (preferenceManager.isFirstRun()) preferenceManager.setFirstRun(false)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id != R.id.newRecipeFragment) {
            navController.popBackStack()
            navController.navigate(R.id.newRecipeFragment)
        }
    }

    private fun navigateTo(destinationId: Int) {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        saveLastFragment(destinationId)
        navController.navigate(destinationId)
    }

    private fun saveLastFragment(fragmentId: Int, recipeId: String? = null) {
        val sharedPreferences = getSharedPreferences("AppStatePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply{
            putInt("last_fragment_id", fragmentId)
            if (recipeId != null) putString("last_recipe_id", recipeId)
            apply()
        }
    }

    private fun navigateToLastFragment() {
        val sharedPreferences = getSharedPreferences("AppStatePrefs", Context.MODE_PRIVATE)
        val lastFragmentId = sharedPreferences.getInt("last_fragment_id", -1)
        val lastRecipeId = sharedPreferences.getString("last_recipe_id", null)

        if (lastFragmentId != -1) {
            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

            if (navController.currentDestination?.id != lastFragmentId) {
                if (lastFragmentId == R.id.displaySavedRecipeFragment && lastRecipeId != null) {
                    openRecipe(lastRecipeId)
                } else {
                    navController.navigate(lastFragmentId)
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    fun setUpActionBarForFragment(fragment: Fragment) {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        binding.toolbar.menu.clear()
        when (fragment) {
            is NewRecipeFragment -> {
                binding.toolbar.setNavigationOnClickListener {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
            is IngredientSelectionFragment, is TemporaryRecipeFragment -> {
                binding.toolbar.setNavigationOnClickListener {
                    navController.popBackStack()
                }
            }
        }
    }

    fun setUpActionBarForTemporaryRecipes(enable: Boolean) {
        binding.toolbar.menu.clear()
        if (enable) {
            val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
            binding.toolbar.inflateMenu(R.menu.return_home_menu)
            binding.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_return_home -> {
                        navController.navigate(
                            R.id.newRecipeFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(navController.graph.startDestinationId, true)
                                .build()
                        )
                        true
                    } else -> false
                }
            }
        }
    }

    fun setUpActionBarForSavedRecipes(recipeId: String) {
        binding.toolbar.menu.clear()
        binding.toolbar.inflateMenu(R.menu.delete_recipe_menu)

        binding.toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_recipe -> {
                    deleteRecipe(recipeId)
                    true
                }
                else -> false
            }
        }
    }
}
