package com.nikdi.recipefyai.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nikdi.recipefyai.R

class IngredientAdapter(
    private val ingredients: MutableList<String>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIngredient: TextView = itemView.findViewById(R.id.ingredientText)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.tvIngredient.text = ingredient

        holder.btnRemove.setOnClickListener {
            onRemoveClick(ingredient) // Call the remove function
        }
    }

    override fun getItemCount(): Int = ingredients.size
}

