package com.madrat.diabeteshelper.ui.diabetesdiary

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.*
import com.madrat.diabeteshelper.logic.util.linearManager
import com.thoughtworks.xstream.XStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.StringReader

class FragmentDiabetesDiary: Fragment() {
    private var nullableBinding: FragmentDiabetesDiaryBinding? = null
    private val binding get() = nullableBinding!!
    private var dropboxClient: DbxClientV2? = null
    private var adapter: DiabetesNotesAdapter? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        nullableBinding = FragmentDiabetesDiaryBinding.inflate(inflater, container, false)
        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()
    
        dropboxClient = DbxClientV2(config, context?.getString(R.string.dropbox_access_token))
        adapter = DiabetesNotesAdapter (
            { position:Int, note: DiabetesNote -> showEditNoteDialog(position, note) },
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
        val listOfDiabetesNotes = arrayListOf(
            DiabetesNote(5.46),
            DiabetesNote(6.66),
            DiabetesNote(7.77),
            DiabetesNote(8.88),
            DiabetesNote(8.89)
        )
        updateListOfDiabetesNotes(listOfDiabetesNotes)
        binding.buttonAddNote.setOnClickListener {
            showAddNoteDialog(view.context)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.button_import_and_export, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.button_import_and_export -> {
                showImportAndExportDialog(requireContext())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun showAddNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogAddDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            buttonAdd.setOnClickListener {
                val currentSugarLevel = editSugarLevel.text.toString().toDouble()
                addDiabetesNoteToList(currentSugarLevel)
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
    private fun showEditNoteDialog(position: Int, diabetesNote: DiabetesNote) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogEditDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            editSugarLevel.setText(diabetesNote.sugarLevel.toString())
            buttonSave.setOnClickListener {
                updateDiabetesNoteValue(
                    position,
                    DiabetesNote(
                        editSugarLevel.text.toString().toDouble()
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
                removeDiabetesNoteFromList(position)
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
    private fun removeDiabetesNoteFromList(position: Int) {
        adapter?.removeNote(position)
        binding.recyclerView.adapter = adapter
    }
    private fun updateDiabetesNoteValue(position: Int, diabetesNote: DiabetesNote) {
        adapter?.updateNote(position, diabetesNote)
        binding.recyclerView.adapter = adapter
    }
    private fun addDiabetesNoteToList(currentSugarLevel: Double) {
        adapter?.addNote(
            DiabetesNote(
                currentSugarLevel
            )
        )
        binding.recyclerView.adapter = adapter
    }
    private fun updateListOfDiabetesNotes(listOfDiabetesNotes: ArrayList<DiabetesNote>) {
        adapter?.updateList(listOfDiabetesNotes)
        binding.recyclerView.adapter = adapter
    }
    
    // Import and Export
    private fun showImportAndExportDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogImportAndExportBinding.inflate(LayoutInflater.from(context))
        val dialog = builder.create()
        with(dialogLayoutBinding) {
            buttonImport.setOnClickListener {
                dialog.dismiss()
                showSelectImportTypeDialog(context)
            }
            buttonExport.setOnClickListener {
            
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
    private fun showSelectImportTypeDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogSelectImportTypeBinding.inflate(LayoutInflater.from(context))
        val dialog = builder.create()
        with(dialogLayoutBinding) {
            buttonImportFromAppDirectory.setOnClickListener {
                dialog.dismiss()
                showImportFromAppDirectoryDialog(context)
            }
            buttonImportFromDropbox.setOnClickListener {
                dialog.dismiss()
                showImportFromDropboxDialog()
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
    // Import from Dropbox
    private fun showImportFromAppDirectoryDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogImportFileFromSourceBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            lateinit var extensionName: String
            
            lateinit var currentExtension: Extension
            
            val pathToDataFolder = context.filesDir.path + "/"
            
            chipGroup.setOnCheckedChangeListener { _, checkedId ->
                extensionName = when(checkedId) {
                    R.id.chip_csv -> {
                        currentExtension = Extension.CSV
                        getString(R.string.extension_csv)
                    }
                    R.id.chip_xml -> {
                        currentExtension = Extension.XML
                        getString(R.string.extension_xml)
                    }
                    R.id.chip_json -> {
                        currentExtension = Extension.JSON
                        getString(R.string.extension_json)
                    }
                    else -> getString(R.string.extension_json)
                }
            }
            
            buttonImportFile.setOnClickListener {
                dialog.dismiss()
                loadFileFromAppDirectory(
                    pathToDataFolder,
                    editFilename.text.toString() + extensionName,
                    currentExtension
                )
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun loadFileFromAppDirectory(
        pathToDir: String,
        pathToFile: String,
        fileExtension: Extension
    ): Disposable? {
        return Single.fromCallable {
            convertFileIntoString(
                pathToDir,
                pathToFile
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                result?.let {
                    updateListWithDeserializedValues(result, fileExtension)
                }
            }, { throwable ->
                throwable.printStackTrace()
            })
    }
    private fun updateListWithDeserializedValues(fileAsString: String, fileExtension: Extension) {
        val deserializedData: List<DiabetesNote> = when(fileExtension) {
            Extension.CSV -> {
                deserializeCsv(fileAsString)
            }
            Extension.XML -> {
                deserializeXml(fileAsString)
            }
            Extension.JSON -> {
                deserializeJson(fileAsString)
            }
        }
        
        updateListOfDiabetesNotes(
            ArrayList(
                deserializedData
            )
        )
    }
    private fun deserializeCsv(csvString: String): List<DiabetesNote> {
        val list = ArrayList<DiabetesNote>()
        
        try {
            val reader = StringReader(csvString)
            
            val records: Iterable<CSVRecord> = CSVFormat.DEFAULT.parse(
                reader
            )
            
            for (record in records) {
                list.add(
                    DiabetesNote(
                        record[0].toDouble()
                    )
                )
            }
            
            reader.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
        
        return list
    }
    private fun deserializeXml(xmlString: String): List<DiabetesNote> {
        val xStream = XStream()
        
        return xStream.fromXML(xmlString) as List<DiabetesNote>
    }
    private fun deserializeJson(jsonString: String): List<DiabetesNote> {
        val gson = Gson()
        val listType = object : TypeToken<List<DiabetesNote>>() { }.type
        return gson.fromJson(jsonString, listType)
    }
    private fun convertFileIntoString(
        pathToDir: String,
        pathToFile: String
    ): String? {
        return try {
            val file = File(pathToDir, pathToFile)
            if (file.exists()) {
                file.bufferedReader().readText()
            } else {
                null
            }
        } catch (exception: IOException) {
            exception.printStackTrace()
            null
        }
    }
    private fun getFileDisposable(filePath: String, fileExtension: Extension): Disposable? {
        return Single.fromCallable {
            downloadFileFromServer(filePath)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ result ->
                result?.let { updateListWithDeserializedValues(result, fileExtension) }
            }
    }
    private fun showImportFromDropboxDialog() {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogImportFileFromSourceBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            lateinit var extensionName: String
            lateinit var currentExtension: Extension
    
            chipGroup.setOnCheckedChangeListener { _, checkedId ->
                extensionName = when(checkedId) {
                    R.id.chip_csv -> {
                        currentExtension = Extension.CSV
                        getString(R.string.extension_csv)
                    }
                    R.id.chip_xml -> {
                        currentExtension = Extension.XML
                        getString(R.string.extension_xml)
                    }
                    R.id.chip_json -> {
                        currentExtension = Extension.JSON
                        getString(R.string.extension_json)
                    }
                    else -> getString(R.string.extension_json)
                }
            }
            
            buttonImportFile.setOnClickListener {
                dialog?.dismiss()
                handleImportFromDropbox(
                    editFilename.text.toString(),
                    extensionName,
                    currentExtension
                )
            }
        }
        with(dialog) {
            this?.window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            this?.setView(dialogLayoutBinding.root)
            this?.show()
        }
    }
    private fun handleImportFromDropbox(fileName: String, extension: String, fileExtension: Extension) {
        val finalFilename = context?.getString(
            R.string.pattern_filename,
            fileName,
            extension
        )
        
        finalFilename?.let { getFileDisposable(it, fileExtension) }
    }
    private fun downloadFileFromServer(filePath: String): String? {
        return dropboxClient?.files()
            ?.download(filePath)
            ?.inputStream
            ?.bufferedReader()
            ?.use(BufferedReader::readText)
    }
}