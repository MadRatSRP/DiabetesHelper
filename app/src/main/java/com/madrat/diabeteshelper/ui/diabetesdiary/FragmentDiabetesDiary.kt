package com.madrat.diabeteshelper.ui.diabetesdiary

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
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
import com.madrat.diabeteshelper.ui.mainactivity.MainActivity
import com.thoughtworks.xstream.XStream
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.exp
import kotlin.system.exitProcess


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
        
            val records: Iterable<CSVRecord> = CSVFormat.DEFAULT
                .withHeader("SugarLevel")
                .parse(
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
    fun doExportOperationByExportType(exportType: ExportType) {
        when(exportType) {
            ExportType.APP_DIRECTORY -> {
                context?.let { showExportToAppDirectoryDialog(it) }
            }
            ExportType.DROPBOX -> {
                context?.let { showExportToDropboxDialog(it) }
            }
            ExportType.EMAIL -> {
            
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
                adapter?.getDiabetesNotes()?.let { diabetesNotes ->
                    handleSaveToDirectory(
                        editFilename.text.toString() + extensionName,
                        pathToDataFolder + editFilename.text.toString() + extensionName,
                        currentExtension,
                        diabetesNotes
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
    private fun handleSaveToDirectory(
        fileName: String,
        finalPathToFile: String,
        fileExtension: Extension,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        when(fileExtension) {
            Extension.CSV -> {
                tryToSerializeCsvAndSaveToFile(
                    finalPathToFile,
                    diabetesNotes
                )
            }
            Extension.XML -> {
                tryToSerializeXmlAndSaveToFile(
                    fileName,
                    diabetesNotes
                )
            }
            Extension.JSON -> {
                tryToSerializeJsonAndSaveToFile(
                    fileName,
                    diabetesNotes
                )
            }
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
                adapter?.getDiabetesNotes()?.let { diabetesNotes ->
                    handleSaveToDropbox(
                        editFilename.text.toString() + extensionName,
                        pathToDataFolder + editFilename.text.toString() + extensionName,
                        currentExtension,
                        diabetesNotes
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
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        when(fileExtension) {
            Extension.CSV -> {
                tryToUploadCSVToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    diabetesNotes
                )
            }
            Extension.XML -> {
                tryToUploadXMLToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    diabetesNotes
                )
            }
            Extension.JSON -> {
                tryToUploadJSONToDropbox(
                    fileName,
                    pathToFileWithFilename,
                    diabetesNotes
                )
            }
        }
    }
    fun uploadFileToDropbox(
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
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeCSV(
                    pathToFileWithFilename,
                    diabetesNotes
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
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeXML(
                    fileName,
                    diabetesNotes
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
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                // save file to user directory
                serializeJSON(
                    fileName,
                    diabetesNotes
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
    fun serializeCSV(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val writer = Files.newBufferedWriter(Paths.get(pathToFile))
        val csvPrinter = CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("SugarLevel"))
        diabetesNotes.forEach { note ->
            csvPrinter.printRecord(note.sugarLevel)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }
    private fun tryToSerializeCsvAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                serializeCSV(
                    pathToFile,
                    diabetesNotes
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
    fun serializeXML(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val serializedString = XStream().toXML(diabetesNotes)
        context?.openFileOutput(pathToFile, Context.MODE_PRIVATE).use {
            it?.write(serializedString.toByteArray())
        }
    }
    private fun tryToSerializeXmlAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                serializeXML(
                    pathToFile,
                    diabetesNotes
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
    fun serializeJSON(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val serializedString = Json.encodeToString(diabetesNotes)
        context?.openFileOutput(pathToFile, Context.MODE_PRIVATE).use {
            it?.write(serializedString.toByteArray())
        }
    }
    private fun tryToSerializeJsonAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                serializeJSON(
                    pathToFile,
                    diabetesNotes
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
    
    private fun printProgress(uploaded: Long, size: Long) {
        System.out.printf(
            "Uploaded %12d / %12d bytes (%5.2f%%)\n",
            uploaded,
            size,
            100 * (uploaded / size.toDouble())
        )
    }
}