package com.nikdi.recipefyai.apirel

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

interface RecipeRequester {
    @POST("generate")
    fun generateRecipe(@Body request: RecipeRequest): Call<RecipeResponse>
}

// Request Model
data class RecipeRequest(
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("servings") val servings: Int,
    @SerializedName("portion_size") val portionSize: Float
)

// Response Model
data class RecipeResponse(
    @SerializedName("markdown") val markdown: String,
)