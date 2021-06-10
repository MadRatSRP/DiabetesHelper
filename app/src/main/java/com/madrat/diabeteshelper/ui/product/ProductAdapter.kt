package com.madrat.diabeteshelper.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.databinding.ListFoodNotesBinding
import com.madrat.diabeteshelper.databinding.ListProductsBinding
import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote

class ProductAdapter(): RecyclerView.Adapter<ProductAdapter.ProductHolder>() {
    private val listOfProducts = ArrayList<Product>()
    
    fun updateList(newListOfProducts: ArrayList<Product>) {
        listOfProducts.clear()
        listOfProducts.addAll(newListOfProducts)
        this.notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val binding = ListProductsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ProductHolder, position: Int)
        = holder.bind(listOfProducts[position])
    
    override fun getItemCount(): Int
        = listOfProducts.size
    
    inner class ProductHolder(private val binding: ListProductsBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            with(binding) {
                productName.text = product.name
                proteins.text = product.proteins.toString()
                fats.text = product.fats.toString()
                carbohydrates.text = product.carbohydrates.toString()
                calories.text = product.calories.toString()
            }
        }
    }
}