package com.madrat.diabeteshelper.ui.diabetesdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.databinding.ListDiabetesNotesBinding
import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote

class DiabetesNotesAdapter(
    private val editNoteListener: (DiabetesNote) -> Unit,
    private val removeNoteListener: (Int) -> Unit
): RecyclerView.Adapter<DiabetesNotesAdapter.DiabetesNotesHolder>() {
    private val listOfDiabetesNotes = ArrayList<DiabetesNote>()
    
    fun getNotes()
        = listOfDiabetesNotes
    
    fun updateNote(diabetesNote: DiabetesNote) {
        val previousNote = listOfDiabetesNotes.find {
            it.id == diabetesNote.id
        }
        
        val position = listOfDiabetesNotes.indexOf(previousNote)
        
        listOfDiabetesNotes[position] = diabetesNote
        
        notifyDataSetChanged()
    }
    
    fun removeNote(noteId: Int) {
        listOfDiabetesNotes.removeIf {
            it.id == noteId
        }
        notifyDataSetChanged()
    }
    
    fun addNote(diabetesNote: DiabetesNote) {
        listOfDiabetesNotes.add(diabetesNote)
        notifyDataSetChanged()
    }
    
    fun updateList(newListOfDiabetesNotes: ArrayList<DiabetesNote>) {
        listOfDiabetesNotes.clear()
        listOfDiabetesNotes.addAll(newListOfDiabetesNotes)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiabetesNotesHolder {
        val binding = ListDiabetesNotesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiabetesNotesHolder(binding)
    }

    override fun onBindViewHolder(holder: DiabetesNotesHolder, position: Int)
            = holder.bind(listOfDiabetesNotes[position])

    override fun getItemCount(): Int
            = listOfDiabetesNotes.size

    inner class DiabetesNotesHolder(private val binding: ListDiabetesNotesBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(diabetesNote: DiabetesNote) {
            with(binding) {
                sugarLevel.text = diabetesNote.sugarLevel.toString()
                buttonEditNote.setOnClickListener {
                    editNoteListener(diabetesNote)
                }
                buttonRemoveNote.setOnClickListener {
                    removeNoteListener(diabetesNote.id)
                }
            }
        }
    }
}