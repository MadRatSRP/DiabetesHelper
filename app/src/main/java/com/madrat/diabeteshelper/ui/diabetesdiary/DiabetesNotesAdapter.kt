package com.madrat.diabeteshelper.ui.diabetesdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.databinding.ListDiabetesNotesBinding
import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import java.util.*
import kotlin.collections.ArrayList

class DiabetesNotesAdapter(
    private val editNoteListener: (DiabetesNote) -> Unit,
    private val removeNoteListener: (Int) -> Unit
): RecyclerView.Adapter<DiabetesNotesAdapter.DiabetesNotesHolder>() {
    private val listOfDiabetesNotes = ArrayList<DiabetesNote>()
    
    val listCopy = ArrayList<DiabetesNote>()
    
    fun removeAll() {
        listOfDiabetesNotes.clear()
        this.notifyDataSetChanged()
    }
    
    fun sortNotesByGlucose() {
        listOfDiabetesNotes.sortBy { it.glucoseLevel }
        this.notifyDataSetChanged()
    }
    
    fun sortNotesByDate() {
        listOfDiabetesNotes.sortBy { it.noteDate }
        this.notifyDataSetChanged()
    }
    
    fun sortNotesByTime() {
        listOfDiabetesNotes.sortBy { it.noteTime }
        this.notifyDataSetChanged()
    }
    
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
        listCopy.addAll(newListOfDiabetesNotes)
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
    
    fun getFilter(): Filter {
        return glucoseFilter
    }
    
    private val glucoseFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: ArrayList<DiabetesNote> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                //listOfDiabetesNotes.let { filteredList.addAll(it) }
                listCopy.let { filteredList.addAll(it) }
            } else {
                val query = constraint.toString().trim().toLowerCase()
                listCopy.forEach {
                    if (it.glucoseLevel.toString().lowercase(Locale.ROOT).contains(query)) {
                        filteredList.add(it)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }
        
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results?.values is ArrayList<*>) {
                listOfDiabetesNotes.clear()
                listOfDiabetesNotes.addAll(results.values as ArrayList<DiabetesNote>)
                notifyDataSetChanged()
            }
        }
    }
    
    inner class DiabetesNotesHolder(private val binding: ListDiabetesNotesBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(diabetesNote: DiabetesNote) {
            with(binding) {
                sugarLevel.text = diabetesNote.glucoseLevel.toString()
                buttonEditNote.setOnClickListener {
                    editNoteListener(diabetesNote)
                }
                buttonRemoveNote.setOnClickListener {
                    removeNoteListener(diabetesNote.id)
                }
                noteDate.text = diabetesNote.noteDate
                noteTime.text = diabetesNote.noteTime
            }
        }
    }
}