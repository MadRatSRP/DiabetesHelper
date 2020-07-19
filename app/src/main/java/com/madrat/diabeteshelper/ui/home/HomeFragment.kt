package com.madrat.diabeteshelper.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.madrat.diabeteshelper.*
import com.madrat.diabeteshelper.databinding.FragmentHomeBinding
import com.madrat.diabeteshelper.logic.model.Home
import com.madrat.diabeteshelper.logic.util.linearManager

class HomeFragment: Fragment(), HomeMVP.View {
    //private val binding by viewBinding(FragmentHomeBinding::bind)

    // ViewBinding variables
    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private var presenter: HomePresenter? = null

    private var adapter: HomeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.home_title)

        // ViewBinding initialization
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = HomeAdapter()

        binding.recyclerView.linearManager()
        binding.recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMVP()

        val listOfHomes = HomeFragmentArgs.fromBundle(requireArguments()).listOfHomes

        if (listOfHomes == null) {
            presenter?.setListOfHomes()
        } else {
            updateListOfHomes(listOfHomes.toCollection(ArrayList()))
        }

        //presenter?.getDisplayNameDisposable()

        //context?.let { presenter?.getMetadataDisposable(it, "Собачка села на травку и сказала гав-гав") }

        //presenter?.getFileDisposable("/test.txt")

        binding.saveAndExportButton.setOnClickListener {
            showSaveAndExportDialog(view)
        }
    }

    override fun setupMVP(){
        presenter = HomePresenter(this, HomeRepository())
    }

    override fun showDisplayName(displayName:String) {
        println(displayName)
    }
    /*override fun showFileMetadata(metadata: FileMetadata) {
        println(metadata.toStringMultiline())
    }*/
    override fun printFileContentToConsole(fileContent: String) {
        println(fileContent)
    }

    override fun showSaveAndExportDialog(view: View) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        val dialogView = View.inflate(context,
            R.layout.dialog_setup_file_format,null)
        builder.setView(dialogView)

        /*val newType: EditText = dialogView.findViewById(R.id.setup_contact_type)
        val newValue: EditText = dialogView.findViewById(R.id.setup_contact_name)
        val buttonCancel: Button = dialogView.findViewById(R.id.cancel_button)
        val buttonYes: Button = dialogView.findViewById(R.id.add_contact_button)*/

        val checkBoxCSV: CheckBox = dialogView.findViewById(R.id.checkBoxCSV)
        val checkBoxXML: CheckBox = dialogView.findViewById(R.id.checkBoxXML)
        val checkBoxJSON: CheckBox = dialogView.findViewById(R.id.checkBoxJSON)


        val exportButton: Button = dialogView.findViewById(R.id.export_button)

        val alertDialog = builder.create()
        alertDialog.show()

        exportButton.setOnClickListener {
            val listOfExtensions = ArrayList<String>()

            if (checkBoxCSV.isChecked) {
                listOfExtensions.add(".csv")
            } else {
                listOfExtensions.remove(".csv")
            }

            if (checkBoxXML.isChecked) {
                listOfExtensions.add(".xml")
            } else {
                listOfExtensions.remove(".xml")
            }

            if (checkBoxJSON.isChecked) {
                listOfExtensions.add(".json")
            } else {
                listOfExtensions.remove(".json")
            }

            val listOfHomes = adapter?.getListOfHomes()?.toTypedArray()

            val action = HomeFragmentDirections.actionHomeViewToExportView(
                listOfExtensions.toTypedArray(), listOfHomes!!
            )

            view.findNavController().navigate(action)

            alertDialog.dismiss()
        }
    }

    override fun updateListOfHomes(listOfHomes: ArrayList<Home>) {
        adapter?.updateListOfHomes(listOfHomes)
        binding.recyclerView.adapter = adapter
    }
}