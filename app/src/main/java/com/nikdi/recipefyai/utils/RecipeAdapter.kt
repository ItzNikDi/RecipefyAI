package com.nikdi.recipefyai.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nikdi.recipefyai.R

class RecipeAdapter(
    private val recipes: MutableList<String>,
    private val onItemClick: (String) -> Unit  // Click listener
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeName: TextView = view.findViewById(R.id.recipeName)

        init {
            view.setOnClickListener {
                onItemClick(recipes[adapterPosition]) // Handle item click
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.recipeName.text = recipes[position]
    }

    override fun getItemCount(): Int = recipes.size

    fun addRecipe(recipe: String) {
        recipes.add(recipe)
        notifyItemInserted(recipes.size - 1)
    }
}
