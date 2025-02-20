package com.nikdi.recipefyai.dbrel

data class Recipe(
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val servings: Int,
    val portionSize: Float,
    val preparation: String,
    val createdAt: Long,
    val editedAt: Long
)