package com.madrat.diabeteshelper.ui.export

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.madrat.diabeteshelper.MyApp
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentExportBinding
import com.madrat.diabeteshelper.hideKeyboardAndClearFocus
import com.madrat.diabeteshelper.logic.Home
import de.siegmar.fastcsv.writer.CsvWriter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets


class ExportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentExportBinding? = null
    private val binding get() = mBinding!!

    private var listOfExtensions: ArrayList<String>? = null

    var client: DbxClientV2? = null

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

        binding.setupFilename.hideKeyboardAndClearFocus {  }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dropboxKey = "cPkFw615PeAAAAAAAAAAYL7uycEhVhzImiCk1DTl-lJU7VUexoBkDxxHveCquhx4"

        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()

        client = DbxClientV2(config, dropboxKey)

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

        binding.saveToDropboxButton.setOnClickListener {
            saveFilesToDropbox(listOfNames!!)
        }
    }

    fun saveFilesToDropbox(listOfHomes: List<Home>) {
        if (listOfExtensions?.contains(".csv")!!) {
            saveFileAsCSVToDropbox(binding.setupFilename.text.toString(), listOfHomes)
        }
    }

    fun saveFilesToUserDevice(listOfHomes: List<Home>) {
        if (listOfExtensions?.contains(".csv")!!) {
            saveFileAsCSVToUserDevice(binding.setupFilename.text.toString(), listOfHomes)
        }
    }

    fun saveFileAsCSVToDropbox(fileName: String, listOfHomes: List<Home>) {
        val filesDirectoryPath = context?.filesDir.toString()

        val fileNameWithExtension = context?.getString(
            R.string.pattern_csv, fileName
        )

        val filePath = filesDirectoryPath + fileNameWithExtension

        val csvWriter = CsvWriter()

        val data: MutableCollection<Array<String>> = ArrayList()
        data.add(arrayOf("author", "value"))

        listOfHomes.forEach { home->
            data.add(arrayOf(home.author, home.value))
        }

        csvWriter.write(File(filePath), StandardCharsets.UTF_8, data)

        val file = File(filePath)

        getMetadataDisposable(fileNameWithExtension!!, file)
    }

    fun getMetadataDisposable(fileName: String, file: File): @NonNull Disposable? {
        return Observable.fromCallable {
            saveStringAsFile(fileName, file)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun saveStringAsFile(fileName: String, file: File) {
        // Upload "test.txt" to Dropbox
        FileInputStream(file).use { `in` ->
            client!!.files()
                .uploadBuilder(fileName)
                .uploadAndFinish(`in`)
        }
    }

    fun saveFileAsCSVToUserDevice(fileName: String,
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