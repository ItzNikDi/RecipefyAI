package com.nikdi.recipefyai.dbrel

data class Recipe(
    val id: Int,
    val name: String,
    val ingredients: String,
    val servings: Int,
    val portionSize: Float,
    val preparation: String
)