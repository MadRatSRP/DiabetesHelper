package com.madrat.diabeteshelper.ui.imports

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.FragmentImportBinding
import com.madrat.diabeteshelper.hideKeyboardAndClearFocus
import com.madrat.diabeteshelper.logic.Home
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.InputStream

class ImportFragment: Fragment() {
    // ViewBinding variables
    private var mBinding: FragmentImportBinding? = null
    private val binding get() = mBinding!!

    var client: DbxClientV2? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // ViewBinding initialization
        mBinding = FragmentImportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dropboxKey = "cPkFw615PeAAAAAAAAAAYL7uycEhVhzImiCk1DTl-lJU7VUexoBkDxxHveCquhx4"

        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()

        client = DbxClientV2(config, dropboxKey)

        binding.importFromUserDeviceButton.setOnClickListener {

        }

        binding.importFromDropboxButton.setOnClickListener {
            showSaveAndExportDialog()
        }

        binding.skipAndSetDefaultValuesButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_import_view_to_home_view)
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

        setAndSaveButton.setOnClickListener {view->
            val fileName = view.context.getString(
                R.string.pattern_filename,
                editText.text.toString(),
                fileExtension
            )

            getFileDisposable(fileName, fileExtension)
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    private fun getFileDisposable(filePath: String,
                                  fileExtension: String): Disposable? {
        return Observable.fromCallable {
            downloadFileFromServer(filePath)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{result ->
                when(fileExtension) {
                    ".csv" -> {

                    }
                    ".xml" -> {
                       // convertJsonToHomeObject(result)
                    }
                    ".json" -> {
                        if (result != null) {
                            deserializeJson(result)
                        }
                    }
                }
            }
    }
    private fun downloadFileFromServer(filePath: String): String {
        return client!!.files()
            .download(filePath)
            .inputStream
            .bufferedReader()
            .use(BufferedReader::readText)
    }
    fun deserializeJson(jsonString: String) {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        /*val listHome = Types.newParameterizedType(
            List::class.java, Home::class.java
        )*/

        val adapter: JsonAdapter<Home> = moshi.adapter(Home::class.java)

        val list = adapter.fromJson(jsonString)

        println(list)
    }
    /*fun convertJsonToHomeObject(jsonString: String) {
        val deserializedValue = jacksonObjectMapper().readerFor(Home::class.java).readValue<Home>(jsonString)

        println(deserializedValue)
    }*/
}