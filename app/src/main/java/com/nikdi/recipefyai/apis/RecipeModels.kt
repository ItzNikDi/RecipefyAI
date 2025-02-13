package com.nikdi.recipefyai.apis

import com.google.gson.annotations.SerializedName

// Request Model
data class RecipeRequest(
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("servings") val servings: Int,
    @SerializedName("units") val units: String
)

// Response Model
data class RecipeResponse(
    @SerializedName("recipe_text") val recipeText: String
)
