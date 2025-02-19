package com.nikdi.recipefyai.dbrel

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RecipeSQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_RECIPES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_INGREDIENTS TEXT,
                $COLUMN_SERVINGS INTEGER,
                $COLUMN_PORTION_SIZE REAL,
                $COLUMN_PREPARATION TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        onCreate(db)
    }

    fun saveRecipe(name: String, ingredients: String, servings: Int, portionSize: Float, preparation: String) {
        val db = writableDatabase
        val insertQuery = """
            INSERT INTO $TABLE_RECIPES ($COLUMN_NAME, $COLUMN_INGREDIENTS, $COLUMN_SERVINGS, $COLUMN_PORTION_SIZE, $COLUMN_PREPARATION) 
            VALUES ('$name', '$ingredients', $servings, $portionSize, '$preparation')
        """
        db.execSQL(insertQuery)
        db.close()
    }

    fun getAllRecipes(): List<Recipe> {
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_RECIPES"
        val cursor = db.rawQuery(selectQuery, null)

        val recipes = mutableListOf<Recipe>()
        if (cursor.moveToFirst()) {
            do {
                // Checking column indexes to ensure they are valid
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val nameIndex = cursor.getColumnIndex(COLUMN_NAME)
                val ingredientsIndex = cursor.getColumnIndex(COLUMN_INGREDIENTS)
                val servingsIndex = cursor.getColumnIndex(COLUMN_SERVINGS)
                val portionSizeIndex = cursor.getColumnIndex(COLUMN_PORTION_SIZE)
                val preparationIndex = cursor.getColumnIndex(COLUMN_PREPARATION)

                // Extract data from the valid columns
                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                val ingredients = cursor.getString(ingredientsIndex)
                val servings = cursor.getInt(servingsIndex)
                val portionSize = cursor.getFloat(portionSizeIndex)
                val preparation = cursor.getString(preparationIndex)

                // Create the Recipe object
                val recipe = Recipe(id, name, ingredients, servings, portionSize, preparation)
                recipes.add(recipe)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return recipes
    }
}