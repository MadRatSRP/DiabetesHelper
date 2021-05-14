package com.madrat.diabeteshelper.ui.fooddiary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.*
import com.madrat.diabeteshelper.linearManager

class FragmentFoodDiary: Fragment() {
    private var nullableBinding: FragmentFoodDiaryBinding? = null
    private val binding get() = nullableBinding!!
    private var adapter: FoodNotesAdapter? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        nullableBinding = FragmentFoodDiaryBinding.inflate(inflater, container, false)
        adapter = FoodNotesAdapter (
            { position:Int, note: FoodNote -> showEditNoteDialog(position, note) },
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
        val listOfFoodNotes = arrayListOf(
            FoodNote("борщ"),
            FoodNote("драники"),
            FoodNote("омлет"),
            FoodNote("котлеты"),
            FoodNote("запеканка")
        )
        updateListOfFoodNotes(listOfFoodNotes)
        binding.buttonAddNote.setOnClickListener {
            showAddNoteDialog(view.context)
        }
    }
    private fun showAddNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogAddFoodNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            buttonAdd.setOnClickListener {
                val mealName = editSugarLevel.text.toString()
                addFoodNoteToList(mealName)
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
    private fun showEditNoteDialog(position: Int, foodNote: FoodNote) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogEditDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            editSugarLevel.setText(foodNote.mealName)
            buttonSave.setOnClickListener {
                updateFoodNoteValue(
                    position,
                    FoodNote(
                        editSugarLevel.text.toString()
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
        val dialogLayoutBinding = DialogRemoveNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            buttonRemoveNote.setOnClickListener {
                removeFoodNoteFromList(position)
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
    private fun removeFoodNoteFromList(position: Int) {
        adapter?.removeNote(position)
        binding.recyclerView.adapter = adapter
    }
    private fun updateFoodNoteValue(position: Int, foodNote: FoodNote) {
        adapter?.updateNote(position, foodNote)
        binding.recyclerView.adapter = adapter
    }
    private fun addFoodNoteToList(mealName: String) {
        adapter?.addNote(
            FoodNote(
                mealName
            )
        )
        binding.recyclerView.adapter = adapter
    }
    private fun updateListOfFoodNotes(listOfFoodNotes: ArrayList<FoodNote>) {
        adapter?.updateList(listOfFoodNotes)
        binding.recyclerView.adapter = adapter
    }
}