package com.madrat.diabeteshelper.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.madrat.diabeteshelper.*
import com.madrat.diabeteshelper.databinding.FragmentHomeBinding
import com.madrat.diabeteshelper.logic.Home

class HomeFragment: Fragment(), HomeMVP.View {
    //private val binding by viewBinding(FragmentHomeBinding::bind)

    // ViewBinding variables
    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private var presenter: HomePresenter? = null

    private var adapter: HomeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

        presenter?.setListOfHomes()

        // Инициализируем клиент Dropbox
        presenter?.initializeDropboxClient(context?.getString(R.string.dropbox_access_token)!!)

        presenter?.getDisplayNameDisposable()

        context?.let { presenter?.getMetadataDisposable(it, "Собачка села на травку и сказала гав-гав") }

        presenter?.getFileDisposable("/test.txt")

        binding.saveAndExportButton.setOnClickListener {
            showSaveAndExportDialog()
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

    fun showSaveAndExportDialog() {
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

        val exportButton: Button = dialogView.findViewById(R.id.export_button)

        val alertDialog = builder.create()
        alertDialog.show()

        exportButton.setOnClickListener {
            if (checkBoxCSV.isChecked) {
                println("CSV")
            }
        }

        /*buttonCancel.setOnClickListener {
            onAddContactDialogCancelButtonClicked(alertDialog)
        }
        buttonYes.setOnClickListener {
            onAddContactDialogYesButtonClicked(
                newType.text.toString().toUpperCase(Locale.getDefault()),
                newValue.text.toString())
        }*/
    }

    override fun updateListOfHomes(listOfHomes: ArrayList<Home>) {
        adapter?.updateListOfHomes(listOfHomes)
        binding.recyclerView.adapter = adapter
    }
}