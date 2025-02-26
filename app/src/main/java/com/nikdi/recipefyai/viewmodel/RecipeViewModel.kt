package com.nikdi.recipefyai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.nikdi.recipefyai.dbrel.AppDatabase
import com.nikdi.recipefyai.dbrel.Recipe
import com.nikdi.recipefyai.dbrel.RecipeRepository
import com.nikdi.recipefyai.dbrel.RecipeSummary
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RecipeRepository

    val recipesLiveData: LiveData<List<RecipeSummary>>

    init {
        val recipeDao = AppDatabase.getDatabase(application).recipeDao()
        repository = RecipeRepository(recipeDao)
        recipesLiveData = repository.recipesLiveData
    }

    fun deleteRecipeById(recipeId: String) {
        viewModelScope.launch {
            repository.deleteRecipeById(recipeId)
        }
    }

    fun getRecipeById(recipeId: String): LiveData<Recipe> {
        return repository.getRecipeById(recipeId)
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.saveRecipe(recipe)
        }
    }
}