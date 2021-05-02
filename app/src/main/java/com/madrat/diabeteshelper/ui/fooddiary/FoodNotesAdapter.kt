package com.madrat.diabeteshelper.ui.fooddiary

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ListFoodNotesBinding
import com.madrat.diabeteshelper.logic.util.inflate
import kotlinx.android.extensions.LayoutContainer

class FoodNotesAdapter(
    private val editNoteListener: (Int, FoodNote) -> Unit,
    private val removeNoteListener: (Int) -> Unit
): RecyclerView.Adapter<FoodNotesAdapter.FoodNotesHolder>() {
    private val listOfFoodNotes = ArrayList<FoodNote>()
    
    fun removeNote(position: Int) {
        listOfFoodNotes.removeAt(position)
        this.notifyDataSetChanged()
    }
    
    fun updateNote(position: Int, foodNote: FoodNote) {
        listOfFoodNotes[position] = foodNote
        this.notifyDataSetChanged()
    }
    
    fun addNote(foodNote: FoodNote) {
        listOfFoodNotes.add(foodNote)
        this.notifyDataSetChanged()
    }
    
    fun updateList(newListOfFoodNotes: ArrayList<FoodNote>) {
        listOfFoodNotes.clear()
        listOfFoodNotes.addAll(newListOfFoodNotes)
        this.notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodNotesHolder
        = FoodNotesHolder(parent.inflate(R.layout.list_food_notes))
    
    override fun onBindViewHolder(holder: FoodNotesHolder, position: Int)
        = holder.bind(listOfFoodNotes[position], position)
    
    override fun getItemCount(): Int
        = listOfFoodNotes.size
    
    inner class FoodNotesHolder internal constructor(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding = ListFoodNotesBinding.bind(containerView)
        
        fun bind(foodNote: FoodNote, position: Int) {
            with(foodNote) {
                binding.mealName.text = mealName
                binding.buttonEditNote.setOnClickListener {
                    editNoteListener(position, foodNote)
                }
                binding.buttonRemoveNote.setOnClickListener {
                    removeNoteListener(position)
                }
            }
        }
    }
}