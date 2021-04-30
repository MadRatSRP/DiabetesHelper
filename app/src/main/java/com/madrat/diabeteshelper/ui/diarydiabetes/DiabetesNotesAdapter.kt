package com.madrat.diabeteshelper.ui.diarydiabetes

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ListDiabetesNotesBinding
import com.madrat.diabeteshelper.databinding.ListOfHomesBinding
import com.madrat.diabeteshelper.logic.Home
import com.madrat.diabeteshelper.logic.util.inflate
import kotlinx.android.extensions.LayoutContainer

class DiabetesNotesAdapter : RecyclerView.Adapter<DiabetesNotesAdapter.DiabetesNotesHolder>() {
    private val listOfDiabetesNotes = ArrayList<DiabetesNote>()

    fun updateListOfDiabetesNotes(newListOfDiabetesNotes: ArrayList<DiabetesNote>) {
        listOfDiabetesNotes.clear()
        listOfDiabetesNotes.addAll(newListOfDiabetesNotes)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiabetesNotesHolder
            = DiabetesNotesHolder(parent.inflate(R.layout.list_diabetes_notes))

    override fun onBindViewHolder(holder: DiabetesNotesHolder, position: Int)
            = holder.bind(listOfDiabetesNotes[position])

    override fun getItemCount(): Int
            = listOfDiabetesNotes.size

    inner class DiabetesNotesHolder internal constructor(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding = ListDiabetesNotesBinding.bind(containerView);

        fun bind(diabetesNote: DiabetesNote) {
            with(diabetesNote) {
                binding.sugarLevel.text = sugarLevel.toString()
            }
        }
    }
}