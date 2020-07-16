package com.madrat.diabeteshelper.ui.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.HomeAdapter
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentExportBinding
import com.madrat.diabeteshelper.databinding.FragmentHomeBinding
import com.madrat.diabeteshelper.linearManager

class ExportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentExportBinding? = null
    private val binding get() = mBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // ViewBinding initialization
        mBinding = FragmentExportBinding.inflate(inflater, container, false)
        val view = binding.root

        val listOfExtensions = arguments?.let {
            ExportFragmentArgs
                .fromBundle(it)
                .listOfExtensions
                .toList()
        }

        binding.amountOfExtensions.text = context?.getString(
            R.string.export_amount_of_extensions, listOfExtensions?.size
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*setupMVP()

        presenter?.setListOfHomes()

        // Инициализируем клиент Dropbox
        presenter?.initializeDropboxClient(context?.getString(R.string.dropbox_access_token)!!)

        presenter?.getDisplayNameDisposable()

        context?.let { presenter?.getMetadataDisposable(it, "Собачка села на травку и сказала гав-гав") }

        presenter?.getFileDisposable("/test.txt")

        binding.saveAndExportButton.setOnClickListener {
            showSaveAndExportDialog()
        }*/
    }
}