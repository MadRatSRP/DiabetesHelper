package com.madrat.diabeteshelper.ui.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dropbox.core.v2.teamlog.AssetLogInfo.file
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentExportBinding
import com.madrat.diabeteshelper.logic.Home
import de.siegmar.fastcsv.writer.CsvWriter
import java.io.File
import java.nio.charset.StandardCharsets


class ExportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentExportBinding? = null
    private val binding get() = mBinding!!

    private var listOfExtensions: ArrayList<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // ViewBinding initialization
        mBinding = FragmentExportBinding.inflate(inflater, container, false)
        val view = binding.root

        listOfExtensions = arguments?.let {
            ExportFragmentArgs
                .fromBundle(it)
                .listOfExtensions
                .toCollection(ArrayList())
        }

        binding.amountOfExtensions.text = context?.getString(
            R.string.export_amount_of_extensions, listOfExtensions?.size
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listOfNames = arguments?.let {
            ExportFragmentArgs
                .fromBundle(it)
                .listOfNames
                .toList()
        }

        println(listOfNames)

        binding.saveToUserDeviceButton.setOnClickListener {
            saveFilesToUserDevice(listOfNames!!)
        }
    }

    fun saveFilesToUserDevice(listOfHomes: List<Home>) {
        val fileName = "example"

        if (listOfExtensions?.contains(".csv")!!) {
            saveFileAsCSV(fileName, listOfHomes)
        }
    }

    fun saveFileAsCSV(fileName: String,
                      listOfHomes: List<Home>) {
        val filesDirPath = context?.filesDir.toString()

        //println(filesDirPath)

        val nameForFileSaving = context?.getString(
            R.string.pattern_csv, fileName
        )

        val pathToFile = filesDirPath + nameForFileSaving

        val csvWriter = CsvWriter()

        val data: MutableCollection<Array<String>> = ArrayList()
        data.add(arrayOf("author", "value"))

        listOfHomes.forEach { home->
            data.add(arrayOf(home.author, home.value))
        }

        csvWriter.write(File(pathToFile), StandardCharsets.UTF_8, data)

        val file = File(pathToFile)

        println(file.readLines())

        //println(file)
    }
}