package com.madrat.diabeteshelper.ui.fooddiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.databinding.ListFoodNotesBinding

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
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodNotesHolder {
        val binding = ListFoodNotesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodNotesHolder(binding)
    }
    
    override fun onBindViewHolder(holder: FoodNotesHolder, position: Int)
        = holder.bind(listOfFoodNotes[position], position)
    
    override fun getItemCount(): Int
        = listOfFoodNotes.size
    
    inner class FoodNotesHolder(private val binding: ListFoodNotesBinding)
        : RecyclerView.ViewHolder(binding.root) {
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