package com.madrat.diabeteshelper.ui.fooddiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.util.IOUtil
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.*
import com.madrat.diabeteshelper.linearManager
import com.madrat.diabeteshelper.network.NetworkClient
import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote
import com.madrat.diabeteshelper.ui.general.ExportType
import com.madrat.diabeteshelper.ui.general.Extension
import com.madrat.diabeteshelper.ui.mainactivity.MainActivity
import com.thoughtworks.xstream.XStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class FragmentFoodDiary: Fragment() {
    private var nullableBinding: FragmentFoodDiaryBinding? = null
    private val binding get() = nullableBinding!!
    private var adapter: FoodNotesAdapter? = null
    private var dropboxClient: DbxClientV2? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        nullableBinding = FragmentFoodDiaryBinding.inflate(inflater, container, false)
        adapter = FoodNotesAdapter (
            { note: FoodNote -> showEditNoteDialog(note) },
            { noteId: Int -> showRemoveNoteDialog(noteId)}
        )
        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()
    
        dropboxClient = DbxClientV2(config, context?.getString(R.string.dropbox_access_token))
        with(binding) {
            recyclerView.adapter = adapter
            recyclerView.linearManager()
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNotesFromServer()
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
    
    private fun loadNotesFromServer() {
        val foodNotesResponse = context?.let {
            NetworkClient.getFoodNotesService(it).getNotes().apply {
                subscribeOn(Schedulers.io())
                observeOn(AndroidSchedulers.mainThread())
            }
        }
        foodNotesResponse?.subscribeWith(object : DisposableSingleObserver<ArrayList<FoodNote>>() {
            override fun onSuccess(list: ArrayList<FoodNote>?) {
                activity?.runOnUiThread {
                    list?.let { updateListOfFoodNotes(it) }
                }
            }
            override fun onError(throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
    }
    private fun showAddNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogAddFoodNoteBinding.inflate(
            LayoutInflater.from(context)
        )
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            buttonAdd.setOnClickListener {
                dialog.dismiss()
                val foodName = editFoodName.text.toString()
                uploadFoodNoteDataToServer(foodName)
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
    private fun uploadFoodNoteDataToServer(foodName: String) {
        val response = context?.let {
            NetworkClient.getFoodNotesService(it).addNote(
                FoodNote(
                    0,
                    foodName
                )
            )
        }
        response?.enqueue(object : Callback<FoodNote> {
            override fun onResponse(
                call: Call<FoodNote>,
                response: Response<FoodNote>
            ) {
                val foodNote: FoodNote? = response.body()
                foodNote?.let { addFoodNoteToList(it) }
            }
            override fun onFailure(
                call: Call<FoodNote>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    private fun addFoodNoteToList(foodNote: FoodNote) {
        adapter?.addNote(foodNote)
        binding.recyclerView.adapter = adapter
    }
    private fun showEditNoteDialog(foodNote: FoodNote) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogEditFoodNoteBinding.inflate(
            LayoutInflater.from(context)
        )
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            editFoodName.setText(foodNote.foodName)
            buttonSave.setOnClickListener {
                dialog?.dismiss()
                updateFoodNoteOnServer(
                    FoodNote(
                        foodNote.noteId,
                        editFoodName.text.toString()
                    )
                )
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
    private fun updateFoodNoteOnServer(foodNote: FoodNote) {
        val response = context?.let {
            NetworkClient.getFoodNotesService(it).updateNote(
                foodNote.noteId,
                foodNote
            )
        }
        response?.enqueue(object : Callback<FoodNote> {
            override fun onResponse(
                call: Call<FoodNote>,
                response: Response<FoodNote>
            ) {
                response.body()?.let { note->
                    updateFoodNoteInList(
                        note
                    )
                }
            }
            override fun onFailure(
                call: Call<FoodNote>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    fun updateFoodNoteInList(foodNote: FoodNote) {
        adapter?.updateNote(foodNote)
        binding.recyclerView.adapter = adapter
    }
    private fun updateListOfFoodNotes(listOfFoodNotes: ArrayList<FoodNote>) {
        adapter?.updateList(listOfFoodNotes)
        binding.recyclerView.adapter = adapter
    }
    private fun showRemoveNoteDialog(noteId: Int) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogRemoveNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            buttonRemoveNote.setOnClickListener {
                dialog?.dismiss()
                removeFoodNoteFromServer(noteId)
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
    private fun removeFoodNoteFromServer(noteId: Int) {
        val response = context?.let {
            NetworkClient.getFoodNotesService(it).deleteNote(
                noteId
            )
        }
        response?.enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                val responseNoteId: Int? = response.body()
                responseNoteId?.let { removeFoodNoteFromList(it) }
            }
            override fun onFailure(
                call: Call<Int>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    private fun removeFoodNoteFromList(position: Int) {
        adapter?.removeNote(position)
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
                dialog.dismiss()
                showSelectExportTypeDialog(context)
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
    // Import
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
                showStatusMessage(
                    R.string.message_successful_file_loaded_from_directory
                )
                result?.let {
                    updateListWithDeserializedValues(
                        result,
                        fileExtension
                    )
                }
            }, { throwable ->
                showStatusMessage(
                    R.string.message_error_file_loaded_from_directory,
                    false
                )
                throwable.printStackTrace()
            })
    }
    private fun updateListWithDeserializedValues(fileAsString: String, fileExtension: Extension) {
        val deserializedData: List<FoodNote> = when(fileExtension) {
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
        
        updateListOfFoodNotes(
            ArrayList(
                deserializedData
            )
        )
    }
    private fun deserializeCsv(csvString: String): List<FoodNote> {
        val list = ArrayList<FoodNote>()
        
        try {
            val reader = StringReader(csvString)
            
            val records: Iterable<CSVRecord> = CSVFormat.DEFAULT
                .withHeader("FoodName")
                .parse(
                    reader
                )
            
            for (record in records) {
                list.add(
                    FoodNote(
                        record[0].toInt(),
                        record[1]
                    )
                )
            }
            
            reader.close()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
        
        return list
    }
    private fun deserializeXml(xmlString: String): List<FoodNote> {
        val xStream = XStream()
        
        return xStream.fromXML(xmlString) as List<FoodNote>
    }
    private fun deserializeJson(jsonString: String): List<FoodNote> {
        return Json.decodeFromString(jsonString)
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
    // Export
    private fun showSelectExportTypeDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogSelectExportTypeBinding.inflate(LayoutInflater.from(context))
        val dialog = builder.create()
        with(dialogLayoutBinding) {
            lateinit var exportType: ExportType
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId) {
                    R.id.radio_export_to_app_directory -> {
                        exportType = ExportType.APP_DIRECTORY
                    }
                    R.id.radio_export_to_dropbox -> {
                        exportType = ExportType.DROPBOX
                    }
                    R.id.radio_export_to_email -> {
                        exportType = ExportType.EMAIL
                    }
                }
            }
            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
            buttonContinue.setOnClickListener {
                dialog.dismiss()
                doExportOperationByExportType(exportType)
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun doExportOperationByExportType(exportType: ExportType) {
        when(exportType) {
            ExportType.APP_DIRECTORY -> {
                context?.let { showExportToAppDirectoryDialog(it) }
            }
            ExportType.DROPBOX -> {
                context?.let { showExportToDropboxDialog(it) }
            }
            ExportType.EMAIL -> {
                context?.let { showExportToEmailDialog(it) }
            }
        }
    }
    private fun showExportToAppDirectoryDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogExportFileToSourceBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            lateinit var extensionName: String
            
            lateinit var currentExtension: Extension
            
            val pathToDataFolder = context.filesDir.path + "/"
            
            title.setText(R.string.hint_export_to_app_directory_type_in_filename)
            
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
            
            buttonExportFile.setOnClickListener {
                dialog.dismiss()
                adapter?.getNotes()?.let { foodNotes ->
                    handleSaveToDirectory(
                        editFilename.text.toString() + extensionName,
                        pathToDataFolder + editFilename.text.toString() + extensionName,
                        currentExtension,
                        foodNotes
                    )
                }
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun showExportToDropboxDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogExportFileToSourceBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            lateinit var extensionName: String
            
            lateinit var currentExtension: Extension
            
            val pathToDataFolder = context.filesDir.path + "/"
            
            title.setText(R.string.hint_export_to_dropbox_type_in_filename)
            
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
            
            buttonExportFile.setOnClickListener {
                dialog.dismiss()
                adapter?.getNotes()?.let { foodNotes ->
                    handleSaveToDropbox(
                        editFilename.text.toString() + extensionName,
                        pathToDataFolder + editFilename.text.toString() + extensionName,
                        currentExtension,
                        foodNotes
                    )
                }
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun handleSaveToDropbox(
        fileName: String,
        pathToFileWithFilename: String,
        fileExtension: Extension,
        foodNotes: ArrayList<FoodNote>
    ) {
        when(fileExtension) {
            Extension.CSV -> {
                tryToUploadCSVToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    foodNotes
                )
            }
            Extension.XML -> {
                tryToUploadXMLToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    foodNotes
                )
            }
            Extension.JSON -> {
                tryToUploadJSONToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    foodNotes
                )
            }
        }
    }
    private fun uploadFileToDropbox(
        fileName: String,
        pathToFileWithFilename: String
    ) {
        val file = File(pathToFileWithFilename)
        dropboxClient?.let { client ->
            uploadFile(
                client,
                file,
                "/$fileName"
            )
        }
        // Remove file after uploading
        file.delete()
    }
    private fun tryToUploadCSVToDropbox(
        fileName: String,
        pathToFileWithFilename: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeCSV(
                    pathToFileWithFilename,
                    foodNotes
                )
                // upload file to dropbox
                uploadFileToDropbox(
                    fileName,
                    pathToFileWithFilename
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showStatusMessage(R.string.message_successful_file_saved_into_directory)
            }, { throwable ->
                throwable.printStackTrace()
                showStatusMessage(R.string.message_error_file_saved_into_directory, false)
            })
    }
    private fun tryToUploadXMLToDropbox(
        fileName: String,
        pathToFileWithFilename: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeXML(
                    fileName,
                    foodNotes
                )
                // upload file to dropbox
                uploadFileToDropbox(
                    fileName,
                    pathToFileWithFilename
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showStatusMessage(R.string.message_successful_file_saved_into_directory)
            }, { throwable ->
                throwable.printStackTrace()
                showStatusMessage(R.string.message_error_file_saved_into_directory, false)
            })
    }
    private fun tryToUploadJSONToDropbox(
        fileName: String,
        pathToFileWithFilename: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeJSON(
                    fileName,
                    foodNotes
                )
                // upload file to dropbox
                uploadFileToDropbox(
                    fileName,
                    pathToFileWithFilename
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showStatusMessage(R.string.message_successful_file_saved_into_directory)
            }, { throwable ->
                throwable.printStackTrace()
                showStatusMessage(R.string.message_error_file_saved_into_directory, false)
            })
    }
    
    private fun showStatusMessage(@StringRes messageRes: Int, isSuccess: Boolean = true) {
        (activity as? MainActivity)?.showMessage(
            messageRes, isSuccess
        )
    }
    
    private fun uploadFile(dbxClient: DbxClientV2, localFile: File, dropboxPath: String) {
        try {
            FileInputStream(localFile).use { stream ->
                val progressListener =
                    IOUtil.ProgressListener { l -> printProgress(l, localFile.length()) }
                val metadata: FileMetadata = dbxClient.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(Date(localFile.lastModified()))
                    .uploadAndFinish(stream, progressListener)
                println(metadata.toStringMultiline())
            }
        } catch (exception: UploadErrorException) {
            System.err.println("Error uploading to Dropbox: " + exception.message)
            exitProcess(1)
        } catch (exception: DbxException) {
            System.err.println("Error uploading to Dropbox: " + exception.message)
            exitProcess(1)
        } catch (exception: IOException) {
            System.err.println("Error reading from file \"" + localFile + "\": " + exception.message)
            exitProcess(1)
        }
    }
    
    private fun showExportToEmailDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogSendEmailBinding.inflate(LayoutInflater.from(context))
        val dialog = builder.create()
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
            buttonSendMessage.setOnClickListener {
                dialog.dismiss()
                val pathToFile = pathToDataFolder + editFilename.text.toString() + extensionName
                adapter?.getNotes()?.let { foodNotes ->
                    handleSaveToDirectory(
                        editFilename.text.toString() + extensionName,
                        pathToFile,
                        currentExtension,
                        foodNotes
                    )
                }
                sendFileToEmail(
                    pathToFile,
                    setupReceiverEmail.text.toString(),
                    setupTopic.text.toString(),
                    setupMessage.text.toString()
                )
            }
        }
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            setView(dialogLayoutBinding.root)
            show()
        }
    }
    private fun sendFileToEmail(
        pathToFile: String,
        emailReceiver: String,
        emailTopic: String,
        emailMessage: String
    ) {
        val file = File(pathToFile)
        val filePathUri = FileProvider.getUriForFile(
            requireContext(),
            "your.application.package.fileprovider",
            file
        )
        
        val emailIntent = Intent(Intent.ACTION_SEND)
        // set the type to 'email'
        // set the type to 'email'
        emailIntent.type = "vnd.android.cursor.dir/email"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailReceiver)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailTopic)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailMessage)
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // the attachment
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, filePathUri)
        requireContext().startActivity(Intent.createChooser(emailIntent, "Send mail..."))
    }
    private fun handleSaveToDirectory(
        fileName: String,
        finalPathToFile: String,
        fileExtension: Extension,
        foodNotes: ArrayList<FoodNote>
    ) {
        when(fileExtension) {
            Extension.CSV -> {
                tryToSerializeCsvAndSaveToFile(
                    finalPathToFile,
                    foodNotes
                )
            }
            Extension.XML -> {
                tryToSerializeXmlAndSaveToFile(
                    fileName,
                    foodNotes
                )
            }
            Extension.JSON -> {
                tryToSerializeJsonAndSaveToFile(
                    fileName,
                    foodNotes
                )
            }
        }
    }
    private fun tryToDoBackgroundSerializationAndSavingToFile(
        backgroundTaskListener: (String, ArrayList<FoodNote>) -> Unit,
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        Single
            .fromCallable {
                backgroundTaskListener(
                    pathToFile,
                    foodNotes
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showStatusMessage(R.string.message_successful_file_saved_into_directory)
            }, { throwable ->
                throwable.printStackTrace()
                showStatusMessage(R.string.message_error_file_saved_into_directory, false)
            })
    }
    private fun tryToSerializeCsvAndSaveToFile(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<FoodNote> ->
                serializeCSV(
                    filePath,
                    notes
                )
            },
            pathToFile,
            foodNotes
        )
    }
    private fun tryToSerializeXmlAndSaveToFile(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<FoodNote> ->
                serializeXML(
                    filePath,
                    notes
                )
            },
            pathToFile,
            foodNotes
        )
    }
    private fun tryToSerializeJsonAndSaveToFile(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<FoodNote> ->
                serializeJSON(
                    filePath,
                    notes
                )
            },
            pathToFile,
            foodNotes
        )
    }
    private fun serializeCSV(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        val writer = Files.newBufferedWriter(Paths.get(pathToFile))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("SugarLevel"))
        foodNotes.forEach { note ->
            csvPrinter.printRecord(note.foodName)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }
    private fun serializeXML(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        val serializedString = XStream()
            .toXML(foodNotes)
        context?.openFileOutput(pathToFile, Context.MODE_PRIVATE).use {
            it?.write(serializedString.toByteArray())
        }
    }
    private fun serializeJSON(
        pathToFile: String,
        foodNotes: ArrayList<FoodNote>
    ) {
        val serializedString = Json.encodeToString(foodNotes)
        context?.openFileOutput(pathToFile, Context.MODE_PRIVATE).use {
            it?.write(serializedString.toByteArray())
        }
    }
    private fun printProgress(uploaded: Long, size: Long) {
        System.out.printf(
            "Uploaded %12d / %12d bytes (%5.2f%%)\n",
            uploaded,
            size,
            100 * (uploaded / size.toDouble())
        )
    }
}