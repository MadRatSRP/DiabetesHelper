package com.madrat.diabeteshelper.ui.export

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.madrat.diabeteshelper.*
import com.madrat.diabeteshelper.databinding.FragmentExportBinding
import com.madrat.diabeteshelper.logic.Home
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths


class ExportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentExportBinding? = null
    private val binding get() = mBinding!!

    private var listOfExtensions: ArrayList<String>? = null

    var client: DbxClientV2? = null

    var args: ExportFragmentArgs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // ViewBinding initialization
        mBinding = FragmentExportBinding.inflate(inflater, container, false)
        val view = binding.root

        args = arguments?.let { ExportFragmentArgs.fromBundle(it) }

        listOfExtensions =
                args!!
                .listOfExtensions
                .toCollection(ArrayList())


        binding.amountOfExtensions.text = context?.getString(
            R.string.export_amount_of_extensions, listOfExtensions?.size
        )

        binding.setupFilename.hideKeyboardAndClearFocus {  }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()

        client = DbxClientV2(config, context?.getString(R.string.dropbox_access_token))

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

        binding.sendEmailButton.setOnClickListener {
            composeEmail()
        }
    }

    // Save to Device
    private fun saveFilesToUserDevice(listOfHomes: List<Home>) {
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
    private fun saveFileAsCSVToUserDevice(fileName: String,
                                          listOfHomes: List<Home>) {
        val csvString = serializeListIntoCSV(listOfHomes)

        createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_csv,
            csvString
        )
    }
    private fun saveFileAsJsonToUserDevice(fileName: String,
                                           listOfHomes: List<Home>) {
        val jsonString = serializeListIntoJSON(listOfHomes)

        createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_json,
            jsonString
        )
    }
    private fun saveFileAsXmlToUserDevice(fileName: String,
                                          listOfHomes: List<Home>) {
        val xmlString = serializeListIntoXML(listOfHomes)

        createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_xml,
            xmlString
        )
    }

    // Save to Dropbox
    private fun saveFilesToDropbox(listOfHomes: List<Home>) {
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
    private fun getMetadataDisposable(fileName: String, file: File): @NonNull Disposable? {
        return Observable.fromCallable {
            saveStringAsFile(fileName, file)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }
    private fun saveStringAsFile(fileName: String, file: File) {
        // Upload "test.txt" to Dropbox
        FileInputStream(file).use { `in` ->
            client!!.files()
                .uploadBuilder(fileName)
                .uploadAndFinish(`in`)
        }
    }
    private fun saveFileAsCSVToDropbox(fileName: String, listOfHomes: List<Home>) {
        val fileNameWithExtension = context?.getString(
            R.string.pattern_csv, fileName
        )

        val pathToFile = getPathToFile(fileNameWithExtension!!)

        try {
            // create a writer
            val writer: Writer = Files.newBufferedWriter(
                Paths.get(requireContext().filesDir.toString() + fileNameWithExtension)
            )

            // write CSV file
            val printer = CSVFormat.DEFAULT.withHeader("author", "value").print(writer)

            // write list to file
            listOfHomes.forEach {
                printer.printRecord(it.author, it.value)
            }

            // flush the stream
            printer.flush()

            // close the writer
            writer.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        getMetadataDisposable(context?.getString(
            R.string.pattern_csv, fileName)!!,
            File(pathToFile))
    }
    private fun saveFileAsJsonToDropbox(fileName: String, listOfHomes: List<Home>) {
        val jsonString = serializeListIntoJSON(listOfHomes)

        val file = createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_json,
            jsonString
        )

        getMetadataDisposable(
            context?.getString(
                R.string.pattern_json, fileName)!!,
            file)
    }
    private fun saveFileAsXmlToDropbox(fileName: String, listOfHomes: List<Home>) {
        val xmlString = serializeListIntoXML(listOfHomes)

        val file = createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_xml,
            xmlString
        )

        getMetadataDisposable(
            context?.getString(
                R.string.pattern_xml, fileName)!!,
            file)
    }

    // Send files with Email
    private fun composeEmail() {
        if (listOfExtensions?.size == 1) {
            var filePath: Uri? = null

            if (listOfExtensions?.contains(".csv")!!) {
                val csvFile = getCSVFile("bober")

                filePath = FileProvider.getUriForFile(
                    requireContext(),
                    "your.application.package.fileprovider",
                    csvFile
                )
            }

            if (listOfExtensions?.contains(".xml")!!) {
                val xmlFile = getXMLFile("bober")

                filePath = FileProvider.getUriForFile(
                    requireContext(),
                    "your.application.package.fileprovider",
                    xmlFile
                )
            }

            if (listOfExtensions?.contains(".json")!!) {
                val jsonFile = getJSONFile("bober")

                filePath = FileProvider.getUriForFile(
                    requireContext(),
                    "your.application.package.fileprovider",
                    jsonFile
                )
            }

            val emailIntent = Intent(Intent.ACTION_SEND)
            // set the type to 'email'
            // set the type to 'email'
            emailIntent.type = "vnd.android.cursor.dir/email"
            val to = arrayOf("mischa.alpeew@yandex.ru")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Scale Data")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "This is the body")
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // the attachment
            // the attachment
            emailIntent.putExtra(Intent.EXTRA_STREAM, filePath)
            requireContext().startActivity(Intent.createChooser(emailIntent, "Send mail..."))
        } else {
            val listOfAttachments = ArrayList<Uri>()

            if (listOfExtensions?.contains(".csv")!!) {
                val csvFile = getCSVFile("bober")

                listOfAttachments.add(
                    FileProvider.getUriForFile(
                        requireContext(),
                        "your.application.package.fileprovider",
                        csvFile)
                )
            }

            if (listOfExtensions?.contains(".xml")!!) {
                val xmlFile = getXMLFile("bober")

                listOfAttachments.add(
                    FileProvider.getUriForFile(
                        requireContext(),
                        "your.application.package.fileprovider",
                        xmlFile
                    )
                )
            }

            if (listOfExtensions?.contains(".json")!!) {
                val jsonFile = getJSONFile("bober")

                listOfAttachments.add(
                    FileProvider.getUriForFile(
                        requireContext(),
                        "your.application.package.fileprovider",
                        jsonFile
                    )
                )
            }

            val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
            // set the type to 'email'
            // set the type to 'email'
            emailIntent.type = "vnd.android.cursor.dir/email"
            val to = arrayOf("mischa.alpeew@yandex.ru")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Scale Data")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "This is the body")
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // the attachment
            // the attachment
            emailIntent.putExtra(Intent.EXTRA_STREAM, listOfAttachments)
            requireContext().startActivity(Intent.createChooser(emailIntent, "Send mail..."))
        }
    }
    private fun getCSVFile(fileName: String): File {
        val listOfHomes = args!!
            .listOfNames
            .toCollection(ArrayList())

        val csvString = serializeListIntoCSV(listOfHomes)

        return createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_csv,
            csvString
        )
    }

    private fun getJSONFile(fileName: String): File {
        val listOfHomes = args!!
            .listOfNames
            .toCollection(ArrayList())

        val jsonString = serializeListIntoJSON(listOfHomes)

        return createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_json, jsonString
        )
    }

    private fun getXMLFile(fileName: String): File {
        val listOfHomes = args!!
            .listOfNames
            .toCollection(ArrayList())

        val xmlString = serializeListIntoXML(listOfHomes)

        return createFileWithExtensionAndWriteContent(
            fileName, R.string.pattern_xml,
            xmlString
        )
    }
}