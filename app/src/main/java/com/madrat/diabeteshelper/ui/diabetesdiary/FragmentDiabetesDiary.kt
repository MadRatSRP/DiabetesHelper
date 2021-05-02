package com.madrat.diabeteshelper.ui.diabetesdiary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.DialogAddDiabetesNoteBinding
import com.madrat.diabeteshelper.databinding.DialogEditDiabetesNoteBinding
import com.madrat.diabeteshelper.databinding.DialogRemoveDiabetesNoteBinding
import com.madrat.diabeteshelper.databinding.FragmentDiabetesDiaryBinding
import com.madrat.diabeteshelper.logic.util.linearManager

class FragmentDiabetesDiary: Fragment() {
    private var nullableBinding: FragmentDiabetesDiaryBinding? = null
    private val binding get() = nullableBinding!!
    private var adapter: DiabetesNotesAdapter? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        nullableBinding = FragmentDiabetesDiaryBinding.inflate(inflater, container, false)
        adapter = DiabetesNotesAdapter (
            { position:Int, note: DiabetesNote -> showEditNoteDialog(position, note) },
            { position:Int -> showRemoveNoteDialog(position)}
        )
        with(binding) {
            recyclerView.adapter = adapter
            recyclerView.linearManager()
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listOfDiabetesNotes = arrayListOf(
            DiabetesNote(5.46),
            DiabetesNote(6.66),
            DiabetesNote(7.77),
            DiabetesNote(8.88),
            DiabetesNote(8.89)
        )
        updateListOfDiabetesNotes(listOfDiabetesNotes)
        binding.buttonAddNote.setOnClickListener {
            showAddNoteDialog(view.context)
        }
    }
    private fun showAddNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogAddDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            buttonAdd.setOnClickListener {
                val currentSugarLevel = editSugarLevel.text.toString().toDouble()
                addDiabetesNoteToList(currentSugarLevel)
                dialog.dismiss()
            }
            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun showEditNoteDialog(position: Int, diabetesNote: DiabetesNote) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogEditDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            editSugarLevel.setText(diabetesNote.sugarLevel.toString())
            buttonSave.setOnClickListener {
                updateDiabetesNoteValue(
                    position,
                    DiabetesNote(
                        editSugarLevel.text.toString().toDouble()
                    )
                )
                dialog?.dismiss()
            }
            buttonCancel.setOnClickListener {
                dialog?.dismiss()
            }
        }
        with(dialog) {
            this?.window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            this?.setView(dialogLayoutBinding.root)
            this?.show()
        }
    }
    private fun showRemoveNoteDialog(position: Int) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogRemoveDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            buttonRemoveNote.setOnClickListener {
                removeDiabetesNoteFromList(position)
                dialog?.dismiss()
            }
            buttonCancel.setOnClickListener {
                dialog?.dismiss()
            }
        }
        with(dialog) {
            this?.window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            this?.setView(dialogLayoutBinding.root)
            this?.show()
        }
    }
    private fun removeDiabetesNoteFromList(position: Int) {
        adapter?.removeDiabetesNote(position)
        binding.recyclerView.adapter = adapter
    }
    private fun updateDiabetesNoteValue(position: Int, diabetesNote: DiabetesNote) {
        adapter?.updateDiabetesNote(position, diabetesNote)
        binding.recyclerView.adapter = adapter
    }
    private fun addDiabetesNoteToList(currentSugarLevel: Double) {
        adapter?.addDiabetesNote(
            DiabetesNote(
                currentSugarLevel
            )
        )
        binding.recyclerView.adapter = adapter
    }
    private fun updateListOfDiabetesNotes(listOfDiabetesNotes: ArrayList<DiabetesNote>) {
        adapter?.updateListOfDiabetesNotes(listOfDiabetesNotes)
        binding.recyclerView.adapter = adapter
    }
}