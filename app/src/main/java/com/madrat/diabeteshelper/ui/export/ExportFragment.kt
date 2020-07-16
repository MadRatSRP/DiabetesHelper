package com.madrat.diabeteshelper.ui.export

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentExportBinding
import com.madrat.diabeteshelper.hideKeyboardAndClearFocus
import com.madrat.diabeteshelper.logic.Home
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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

    // Save to Device
    fun saveFilesToUserDevice(listOfHomes: List<Home>) {
        if (listOfExtensions?.contains(".csv")!!) {
            saveFileAsCSVToUserDevice(binding.setupFilename.text.toString(), listOfHomes)
        }

        if (listOfExtensions?.contains(".json")!!) {
            saveFileAsJsonToUserDevice(binding.setupFilename.text.toString(), listOfHomes)
        }

        if (listOfExtensions?.contains(".xml")!!) {
            saveFileAsXmlToUserDevice(binding.setupFilename.text.toString(), listOfHomes)
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
    fun saveFileAsJsonToUserDevice(fileName: String,
                                   listOfHomes: List<Home>) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter: JsonAdapter<Home> =
            moshi.adapter<Home>(Home::class.java)

        var finalJSON = ""

        listOfHomes.forEach {home->
            val json = jsonAdapter.toJson(home)

            finalJSON += json
        }

        val filesDirectoryPath = context?.filesDir.toString()

        val fileNameWithExtension = context?.getString(
            R.string.pattern_json, fileName
        )

        val filePath = filesDirectoryPath + fileNameWithExtension

        val file = File(filePath)

        file.writeText(finalJSON)

        val check = File(filePath)

        println(check.readLines())
    }
    fun saveFileAsXmlToUserDevice(fileName: String,
                                  listOfHomes: List<Home>) {
        val xmlMapper = XmlMapper()

        var finalString = ""

        listOfHomes.forEach { home->
            finalString += xmlMapper.writeValueAsString(home)
        }

        val filesDirectoryPath = context?.filesDir.toString()

        val fileNameWithExtension = context?.getString(
            R.string.pattern_json, fileName
        )

        val filePath = filesDirectoryPath + fileNameWithExtension

        val file = File(filePath)

        file.writeText(finalString)

        val savedFile = File(filePath)
    }

    // Save to Dropbox
    fun saveFilesToDropbox(listOfHomes: List<Home>) {
        if (listOfExtensions?.contains(".csv")!!) {
            saveFileAsCSVToDropbox(binding.setupFilename.text.toString(), listOfHomes)
        }

        if (listOfExtensions?.contains(".json")!!) {
            saveFileAsJsonToDropbox(binding.setupFilename.text.toString(), listOfHomes)
        }

        if (listOfExtensions?.contains(".xml")!!) {
            saveFileAsXmlToDropbox(binding.setupFilename.text.toString(), listOfHomes)
        }
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
    fun saveFileAsJsonToDropbox(fileName: String, listOfHomes: List<Home>) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val jsonAdapter: JsonAdapter<Home> =
            moshi.adapter<Home>(Home::class.java)

        var finalJSON: String = ""

        listOfHomes.forEach {home->
            val json = jsonAdapter.toJson(home)

            finalJSON += json
        }

        val filesDirectoryPath = context?.filesDir.toString()

        val fileNameWithExtension = context?.getString(
            R.string.pattern_json, fileName
        )

        val filePath = filesDirectoryPath + fileNameWithExtension

        val file = File(filePath)

        file.writeText(finalJSON)

        val savedFile = File(filePath)

        getMetadataDisposable(fileNameWithExtension!!, savedFile)
    }

    fun saveFileAsXmlToDropbox(fileName: String, listOfHomes: List<Home>) {
        val xmlMapper = XmlMapper()

        var finalString = ""

        listOfHomes.forEach { home->
            finalString += xmlMapper.writeValueAsString(home)
        }

        val filesDirectoryPath = context?.filesDir.toString()

        val fileNameWithExtension = context?.getString(
            R.string.pattern_xml, fileName
        )

        val filePath = filesDirectoryPath + fileNameWithExtension

        val file = File(filePath)

        file.writeText(finalString)

        val savedFile = File(filePath)

        getMetadataDisposable(fileNameWithExtension!!, savedFile)
    }
}