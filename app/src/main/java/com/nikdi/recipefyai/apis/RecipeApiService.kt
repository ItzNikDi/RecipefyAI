package com.nikdi.recipefyai.apis

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RecipeApiService {
    @POST("generate")
    fun generateRecipe(@Body request: RecipeRequest): Call<RecipeResponse>
}