package com.nikdi.recipefyai.dbrel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RecipeConstants.TABLE_RECIPES)
data class Recipe(
    @PrimaryKey val id: String,
    @ColumnInfo(name = RecipeConstants.COLUMN_NAME) val name: String,
    @ColumnInfo(name = RecipeConstants.COLUMN_INGREDIENTS) val ingredients: List<String>,
    @ColumnInfo(name = RecipeConstants.COLUMN_SERVINGS) val servings: Int,
    @ColumnInfo(name = RecipeConstants.COLUMN_PORTION_SIZE) val portionSize: Float,
    @ColumnInfo(name = RecipeConstants.COLUMN_PREPARATION) val preparation: String,
    @ColumnInfo(name = RecipeConstants.COLUMN_CREATED) val createdAt: Long,
    @ColumnInfo(name = RecipeConstants.COLUMN_EDITED) val editedAt: Long
)