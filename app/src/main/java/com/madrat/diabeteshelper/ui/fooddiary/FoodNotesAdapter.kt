package com.madrat.diabeteshelper.ui.fooddiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.databinding.ListFoodNotesBinding
import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote

class FoodNotesAdapter(
    private val editNoteListener: (FoodNote) -> Unit,
    private val removeNoteListener: (Int) -> Unit
): RecyclerView.Adapter<FoodNotesAdapter.FoodNotesHolder>() {
    private val listOfFoodNotes = ArrayList<FoodNote>()
    
    fun getNotes()
        = listOfFoodNotes
    
    fun updateNote(foodNote: FoodNote) {
        val previousNote = listOfFoodNotes.find {
            it.id == foodNote.id
        }
        
        val position = listOfFoodNotes.indexOf(previousNote)
        
        listOfFoodNotes[position] = foodNote
        
        notifyDataSetChanged()
    }
    
    fun removeNote(id: Int) {
        listOfFoodNotes.removeIf {
            it.id == id
        }
        notifyDataSetChanged()
    }
    
    fun addNote(foodNote: FoodNote) {
        listOfFoodNotes.add(foodNote)
        notifyDataSetChanged()
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
        = holder.bind(listOfFoodNotes[position])
    
    override fun getItemCount(): Int
        = listOfFoodNotes.size
    
    inner class FoodNotesHolder(private val binding: ListFoodNotesBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodNote: FoodNote) {
            with(binding) {
                mealName.text = foodNote.foodName
                buttonEditNote.setOnClickListener {
                    editNoteListener(foodNote)
                }
                buttonRemoveNote.setOnClickListener {
                    removeNoteListener(foodNote.id)
                }
            }
        }
    }
}