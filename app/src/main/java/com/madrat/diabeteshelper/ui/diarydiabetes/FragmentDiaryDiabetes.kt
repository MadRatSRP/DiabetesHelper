package com.madrat.diabeteshelper.ui.diarydiabetes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.databinding.FragmentDiaryDiabetesBinding
import com.madrat.diabeteshelper.logic.Home
import com.madrat.diabeteshelper.logic.util.linearManager

class FragmentDiaryDiabetes: Fragment() {
    private var nullableBinding: FragmentDiaryDiabetesBinding? = null
    private val binding get() = nullableBinding!!
    private var adapter: DiabetesNotesAdapter? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        nullableBinding = FragmentDiaryDiabetesBinding.inflate(inflater, container, false)
        adapter = DiabetesNotesAdapter()
        with(binding) {
            recyclerView.adapter = adapter
            recyclerView.linearManager()
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listOfDiabetesNotes = arrayListOf(
            DiabetesNote(1, 5.46),
            DiabetesNote(2, 6.66),
            DiabetesNote(3, 7.77),
            DiabetesNote(4, 8.88),
            DiabetesNote(5, 8.89)
        )
        updateListOfDiabetesNotes(listOfDiabetesNotes)
        binding.buttonAddNote.setOnClickListener {

        }
    }
    private fun updateListOfDiabetesNotes(listOfDiabetesNotes: ArrayList<DiabetesNote>) {
        adapter?.updateListOfDiabetesNotes(listOfDiabetesNotes)
        binding.recyclerView.adapter = adapter
    }
}