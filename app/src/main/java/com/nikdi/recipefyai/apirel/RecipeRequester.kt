package com.nikdi.recipefyai.apirel

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

interface RecipeRequester {
    @POST("ingredients")
    fun generateFromIngredients(@Body request: FromIngredientsRequest): Call<RecipeResponse>
    @POST("name")
    fun generateFromName(@Body request: FromNameRequest): Call<RecipeResponse>
}

data class FromIngredientsRequest(
    @SerializedName("ingredients") val ingredients: List<String>,
    @SerializedName("servings") val servings: Int,
    @SerializedName("portion_size") val portionSize: Float
)

data class FromNameRequest(
    @SerializedName("name") val name: String,
    @SerializedName("servings") val servings: Int,
    @SerializedName("portion_size") val portionSize: Float
)

data class RecipeResponse(
    @SerializedName("markdown") val markdown: String,
)