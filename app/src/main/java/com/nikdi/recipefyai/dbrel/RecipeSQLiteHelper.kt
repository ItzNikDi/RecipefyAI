package com.nikdi.recipefyai.dbrel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecipeSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipeDatabase.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_RECIPES = "recipes"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_INGREDIENTS = "ingredients"
        const val COLUMN_SERVINGS = "servings"
        const val COLUMN_PORTION_SIZE = "portion_size"
        const val COLUMN_PREPARATION = "preparation"
        const val COLUMN_CREATED = "created_at"
        const val COLUMN_EDITED = "edited_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
        CREATE TABLE $TABLE_RECIPES (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_NAME TEXT NOT NULL,
            $COLUMN_INGREDIENTS TEXT NOT NULL,
            $COLUMN_SERVINGS INTEGER NOT NULL,
            $COLUMN_PORTION_SIZE REAL NOT NULL,
            $COLUMN_PREPARATION TEXT NOT NULL,
            $COLUMN_CREATED INTEGER NOT NULL,
            $COLUMN_EDITED INTEGER NOT NULL
        )
    """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        onCreate(db)
    }

    fun saveRecipe(recipe: Recipe) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, recipe.id)
            put(COLUMN_NAME, recipe.name)
            put(COLUMN_INGREDIENTS, Gson().toJson(recipe.ingredients)) // Convert list to JSON
            put(COLUMN_SERVINGS, recipe.servings)
            put(COLUMN_PORTION_SIZE, recipe.portionSize)
            put(COLUMN_PREPARATION, recipe.preparation)
            put(COLUMN_CREATED, recipe.createdAt)
            put(COLUMN_EDITED, recipe.editedAt)
        }
        db.insert(TABLE_RECIPES, null, values)
        db.close()
    }

    fun getAllRecipeNamesSorted(): List<RecipeSummary> {
        val recipes = mutableListOf<RecipeSummary>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECIPES, arrayOf(COLUMN_ID, COLUMN_NAME),
            null, null, null, null, "$COLUMN_EDITED DESC"
        )

        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            recipes.add(RecipeSummary(id = id, name = name))
        }

        cursor.close()
        db.close()
        return recipes
    }

    fun getRecipeById(recipeId: String): Recipe {
        val db = readableDatabase
        var recipe: Recipe? = null

        val cursor = db.query(
            TABLE_RECIPES, null, // Select all columns from the recipes table
            "$COLUMN_ID = ?", // WHERE condition
            arrayOf(recipeId), // Bind the recipeId as the selection argument
            null, null, null
        )

        if (cursor.moveToFirst()) {
            // Extract values from cursor and create a Recipe object
            val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val ingredientsJson = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS))
            val servings = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERVINGS))
            val portionSize = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_PORTION_SIZE))
            val preparation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PREPARATION))
            val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED))
            val editedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EDITED))

            // Convert ingredients JSON back into a list
            val ingredientsType = object : TypeToken<List<String>>() {}.type
            val ingredients: List<String> = Gson().fromJson(ingredientsJson, ingredientsType)

            recipe = Recipe(id, name, ingredients, servings, portionSize, preparation, createdAt, editedAt)
        }

        cursor.close()
        db.close()

        return recipe ?: throw IllegalArgumentException("Recipe with ID $recipeId not found")
    }

}
