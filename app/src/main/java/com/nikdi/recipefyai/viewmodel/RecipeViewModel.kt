package com.nikdi.recipefyai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.nikdi.recipefyai.dbrel.AppDatabase
import com.nikdi.recipefyai.dbrel.Recipe
import com.nikdi.recipefyai.dbrel.RecipeDao
import com.nikdi.recipefyai.dbrel.RecipeSummary

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val recipeDao: RecipeDao = AppDatabase.getDatabase(application).recipeDao()

    val recipesLiveData: LiveData<List<RecipeSummary>> = recipeDao.getAllRecipeNamesSorted()

    suspend fun deleteRecipeById(recipeId: String) {
        recipeDao.deleteRecipeById(recipeId)
    }

    fun getRecipeById(recipeId: String): LiveData<Recipe> {
        return recipeDao.getRecipeById(recipeId)
    }

    suspend fun saveRecipe(recipe: Recipe) {
        recipeDao.saveRecipe(recipe)
    }
}