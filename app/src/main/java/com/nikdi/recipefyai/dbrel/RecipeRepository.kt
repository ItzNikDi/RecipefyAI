package com.nikdi.recipefyai.dbrel

import androidx.lifecycle.LiveData

class RecipeRepository(private val recipeDao: RecipeDao) {
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