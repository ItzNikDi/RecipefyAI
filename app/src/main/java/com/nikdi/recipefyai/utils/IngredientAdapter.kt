package com.nikdi.recipefyai.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikdi.recipefyai.databinding.ItemIngredientBinding

class IngredientAdapter(
    private val ingredients: MutableList<String>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(private val binding: ItemIngredientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ingredient: String, position: Int) {
            binding.ingredientText.text = ingredient
            binding.btnRemove.setOnClickListener() {
                onRemoveClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.bind(ingredient, position)
    }

    fun removeIngredient(position: Int) {
        if (position in ingredients.indices) {
            ingredients.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, ingredients.size)
        }
    }

    fun removeDuplicates() {
        ingredients.distinct().let {
            ingredients.clear()
            ingredients.addAll(it)
        }
    }

    override fun getItemCount(): Int = ingredients.size
}

