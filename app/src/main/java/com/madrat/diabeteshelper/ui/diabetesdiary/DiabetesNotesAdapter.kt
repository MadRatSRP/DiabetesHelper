package com.madrat.diabeteshelper.ui.diabetesdiary

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ListDiabetesNotesBinding
import com.madrat.diabeteshelper.logic.util.inflate
import kotlinx.android.extensions.LayoutContainer

class DiabetesNotesAdapter(
    private val editNoteListener: (Int, DiabetesNote) -> Unit,
    private val removeNoteListener: (Int) -> Unit
): RecyclerView.Adapter<DiabetesNotesAdapter.DiabetesNotesHolder>() {
    private val listOfDiabetesNotes = ArrayList<DiabetesNote>()

    fun getDiabetesNotes()
        = listOfDiabetesNotes
    
    fun removeNote(position: Int) {
        listOfDiabetesNotes.removeAt(position)
        this.notifyDataSetChanged()
    }
    
    fun updateNote(position: Int, diabetesNote: DiabetesNote) {
        listOfDiabetesNotes[position] = diabetesNote
        this.notifyDataSetChanged()
    }
    
    fun addNote(diabetesNote: DiabetesNote) {
        listOfDiabetesNotes.add(diabetesNote)
        this.notifyDataSetChanged()
    }
    
    fun updateList(newListOfDiabetesNotes: ArrayList<DiabetesNote>) {
        listOfDiabetesNotes.clear()
        listOfDiabetesNotes.addAll(newListOfDiabetesNotes)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiabetesNotesHolder
            = DiabetesNotesHolder(parent.inflate(R.layout.list_diabetes_notes))

    override fun onBindViewHolder(holder: DiabetesNotesHolder, position: Int)
            = holder.bind(listOfDiabetesNotes[position], position)

    override fun getItemCount(): Int
            = listOfDiabetesNotes.size

    inner class DiabetesNotesHolder internal constructor(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding = ListDiabetesNotesBinding.bind(containerView)

        fun bind(diabetesNote: DiabetesNote, position: Int) {
            with(diabetesNote) {
                binding.sugarLevel.text = sugarLevel.toString()
                binding.buttonEditNote.setOnClickListener {
                    editNoteListener(position, diabetesNote)
                }
                binding.buttonRemoveNote.setOnClickListener {
                    removeNoteListener(position)
                }
            }
        }
    }
}