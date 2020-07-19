package com.madrat.diabeteshelper.ui.imports

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentImportBinding
import com.madrat.diabeteshelper.logic.util.hideKeyboardAndClearFocus
import com.madrat.diabeteshelper.logic.model.Home
import com.thoughtworks.xstream.XStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader


class ImportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentImportBinding? = null
    private val binding get() = mBinding!!

    private var client: DbxClientV2? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.import_title)

        // ViewBinding initialization
        mBinding = FragmentImportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()

        client = DbxClientV2(config, context?.getString(R.string.dropbox_access_token))

        binding.importFromUserDeviceButton.setOnClickListener {

        }

        binding.importFromDropboxButton.setOnClickListener {
            showSaveAndExportDialog()
        }

        binding.skipAndSetDefaultValuesButton.setOnClickListener {
            val action = ImportFragmentDirections.actionImportViewToHomeView(
                null
            )

            view.findNavController().navigate(action)
        }
    }

    // Import from Dropbox
    private fun showSaveAndExportDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        val dialogView = View.inflate(context,
            R.layout.dialog_import_file_from_dropbox,null)
        builder.setView(dialogView)

        val editText: EditText = dialogView.findViewById(R.id.setup_filename)
        val radioGroup: RadioGroup = dialogView.findViewById(R.id.radio_group)
        val setAndSaveButton: Button = dialogView.findViewById(R.id.set_and_move_to_home_button)

        editText.hideKeyboardAndClearFocus {  }

        var fileExtension = ""

        radioGroup.setOnCheckedChangeListener { _, radioButtonId ->
            when(radioButtonId) {
                R.id.radio_csv -> {
                    fileExtension = ".csv"
                }
                R.id.radio_xml -> {
                    fileExtension = ".xml"
                }
                R.id.radio_json -> {
                    fileExtension = ".json"
                }
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()

        setAndSaveButton.setOnClickListener {view->
            val fileName = view.context.getString(
                R.string.pattern_filename,
                editText.text.toString(),
                fileExtension
            )

            getFileDisposable(fileName, fileExtension)

            alertDialog.dismiss()
        }
    }
    private fun getFileDisposable(filePath: String,
                                  fileExtension: String): Disposable? {
        return Observable.fromCallable {
            downloadFileFromServer(filePath)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{result ->
                var listOfHomes: List<Home> = ArrayList()

                when(fileExtension) {
                    ".csv" -> {
                        if (result != null) {
                            listOfHomes = deserializeCsv(result)
                        }
                    }
                    ".xml" -> {
                        if (result != null) {
                            listOfHomes = deserializeXml(result)
                        }
                    }
                    ".json" -> {
                        if (result != null) {
                            listOfHomes = deserializeJson(result)
                        }
                    }
                }

                val action = ImportFragmentDirections.actionImportViewToHomeView(
                    listOfHomes.toTypedArray()
                )
                view?.findNavController()?.navigate(action)
            }
    }
    private fun downloadFileFromServer(filePath: String): String {
        return client!!.files()
            .download(filePath)
            .inputStream
            .bufferedReader()
            .use(BufferedReader::readText)
    }
    private fun deserializeCsv(csvString: String): List<Home> {
        val list = ArrayList<Home>()

        try {
            val reader = StringReader(csvString)

            // read csv file
            val records: Iterable<CSVRecord> = CSVFormat.DEFAULT.parse(
                reader
            )

            for (record in records) {
                list.add(
                    Home(
                        record[0],
                        record[1]
                    )
                )
            }

            reader.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return list
    }
    private fun deserializeXml(xmlString: String): List<Home> {
        val xStream = XStream()

        return xStream.fromXML(xmlString) as List<Home>
    }
    private fun deserializeJson(jsonString: String): List<Home> {
        val gson = Gson()

        val listType = object : TypeToken<List<Home>>() { }.type

        return gson.fromJson<List<Home>>(jsonString, listType)
    }
}