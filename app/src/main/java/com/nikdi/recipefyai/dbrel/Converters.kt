package com.nikdi.recipefyai.dbrel

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromIngredientsList(ingredients: List<String>): String {
        return Gson().toJson(ingredients)
    }

    @TypeConverter
    fun toIngredientsList(data: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(data, listType)
    }
}