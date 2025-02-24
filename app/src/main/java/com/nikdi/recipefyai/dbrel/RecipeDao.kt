package com.nikdi.recipefyai.dbrel

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {
    @Query("DELETE FROM ${RecipeConstants.TABLE_RECIPES} WHERE ${RecipeConstants.COLUMN_ID} = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: Recipe)

    @Query("SELECT * FROM ${RecipeConstants.TABLE_RECIPES} ORDER BY ${RecipeConstants.COLUMN_EDITED} DESC")
    fun getAllRecipeNamesSorted(): LiveData<List<RecipeSummary>>

    @Query("SELECT * FROM ${RecipeConstants.TABLE_RECIPES} WHERE ${RecipeConstants.COLUMN_ID} = :recipeId LIMIT 1")
    fun getRecipeById(recipeId: String): LiveData<Recipe>
}