package com.madrat.diabeteshelper.ui.diabetesdiary

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.util.IOUtil
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.*
import com.madrat.diabeteshelper.getHashcodeFromPreferences
import com.madrat.diabeteshelper.linearManager
import com.madrat.diabeteshelper.network.NetworkClient
import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.RequestAddDiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.RequestUpdateDiabetesNote
import com.madrat.diabeteshelper.ui.general.ExportType
import com.madrat.diabeteshelper.ui.general.Extension
import com.madrat.diabeteshelper.ui.mainactivity.MainActivity
import com.thoughtworks.xstream.XStream
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
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


class FragmentDiabetesDiary: Fragment(), AdapterView.OnItemSelectedListener {
    private var nullableBinding: FragmentDiabetesDiaryBinding? = null
    private val binding get() = nullableBinding!!
    private var dropboxClient: DbxClientV2? = null
    private var adapter: DiabetesNotesAdapter? = null
    private var networkService: DiabetesNotesNetworkInterface? = null
    
    private var diabetesNotes: ArrayList<DiabetesNote>? = null
    
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.selectedItemPosition) {
            1 -> {
                adapter?.sortNotesByGlucose()
            }
            2 -> {
                adapter?.sortNotesByDate()
            }
            3 -> {
                adapter?.sortNotesByTime()
            }
        }
    }
    
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        nullableBinding = FragmentDiabetesDiaryBinding.inflate(
            inflater,
            container,
            false
        )
        
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userHashCode: String? = getHashcodeFromPreferences()
        if (userHashCode == null) {
            showUnauthorizedUserDialog(view.context)
        } else {
            initializeDependencies(view.context)
            loadNotesFromServer(
                userHashCode
            )
        }
    }
    fun initializeDependencies(context: Context) {
        setHasOptionsMenu(true)
        val config = DbxRequestConfig
            .newBuilder("DiabetesHelper")
            .build()
        dropboxClient = DbxClientV2(
            config,
            context.getString(R.string.dropbox_access_token)
        )
        adapter = DiabetesNotesAdapter (
            { note: DiabetesNote -> showEditNoteDialog(note) },
            { noteId:Int -> showRemoveNoteDialog(noteId)}
        )
        networkService = NetworkClient
                .getRetrofit(context)
                .create(DiabetesNotesNetworkInterface::class.java)
        with(binding) {
            recyclerView.adapter = adapter
            recyclerView.linearManager()
            binding.buttonAddNote.setOnClickListener {
                showAddNoteDialog(context)
            }
            binding.buttonFilterList.setOnClickListener {
                val notesCopy = diabetesNotes
                val listOfDates = ArrayList<String>()
                val groupedNotes = notesCopy?.groupBy {it.noteDate}?.entries
                groupedNotes?.forEach {
                    listOfDates.add(it.key)
                }
    
                changeDateClicklistener(listOfDates)
            }
            binding.buttonCancelFilter.setOnClickListener {
                binding.selectedDate.text = ""
    
                diabetesNotes?.let { it1 -> updateListOfDiabetesNotes(it1) }
            }
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        context.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.diabetes_sort_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                binding.spinnerSort.adapter = adapter
            }
        }
    }
    
    fun changeDateClicklistener(listOfDates: ArrayList<String>) {
        val now: Calendar = Calendar.getInstance()
        val dpd: DatePickerDialog = DatePickerDialog.newInstance(
            { _, year, monthOfYear, dayOfMonth ->
                val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru","RU"))
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = simpleDateFormat.format(
                    calendar.time
                )
                
                binding.selectedDate.text = selectedDate
                
                updateListWithFilteredValues(selectedDate)
            },
            now.get(Calendar.YEAR),  // Initial year selection
            now.get(Calendar.MONTH),  // Initial month selection
            now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        )
        dpd.selectableDays = formCalendarArrayFromDates(
            listOfDates
        )
        dpd.show(childFragmentManager, "Datepickerdialog")
    }
    
    fun updateListWithFilteredValues(selectedDate: String) {
        val listCopy = diabetesNotes
        val updatedList = ArrayList<DiabetesNote>()
    
        listCopy?.filter { it.noteDate == selectedDate }?.let {
            updatedList.addAll(
                it
            )
        }
        
        updateListOfDiabetesNotes(
            updatedList
        )
    }
    
    fun formCalendarArrayFromDates(listOfDates: ArrayList<String>): Array<java.util.Calendar> {
        val calendars = ArrayList<java.util.Calendar>()
        val simpleDateFormat = SimpleDateFormat("dd.MM.yy", Locale("ru","RU"))
        
        listOfDates.forEach {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = simpleDateFormat.parse(it)
            calendars.add(calendar)
        }
        
        return calendars.toTypedArray()
    }
    
    
    private fun showUnauthorizedUserDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle(R.string.title_unathorized)
        builder.setMessage(R.string.message_unathorized)
        builder.setPositiveButton("ОК") {
                dialogInterface, _ ->
            dialogInterface.dismiss()
            findNavController().navigate(R.id.action_navigation_diary_diabetes_to_navigation_user)
        }
        val dialog: AlertDialog = builder.create()
        with(dialog) {
            window?.setBackgroundDrawableResource(R.drawable.rounded_rectrangle_gray)
            show()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_diabetes_notes, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.button_import_and_export -> {
                showImportAndExportDialog(requireContext())
                return true
            }
            R.id.button_show_statistics -> {
                val action = adapter?.getNotes()?.toTypedArray()?.let { it1 ->
                    FragmentDiabetesDiaryDirections.actionNavigationDiaryDiabetesToNavigationDiabetesStatistics(
                        it1
                    )
                }
                action?.let { it1 -> findNavController().navigate(it1) }
                return true
            }
            R.id.button_clear_list -> {
                adapter?.removeAll()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun loadNotesFromServer(userHashcode: String) {
        val response = context?.let {
            networkService?.getNotes(
                userHashcode
            )?.apply {
                subscribeOn(Schedulers.io())
                observeOn(AndroidSchedulers.mainThread())
            }
        }
        response?.subscribeWith(object : DisposableSingleObserver<ArrayList<DiabetesNote>>() {
            override fun onSuccess(notes: ArrayList<DiabetesNote>?) {
                activity?.runOnUiThread {
                    notes?.let { updateListOfDiabetesNotes(it) }
                    diabetesNotes = notes
                }
            }
            override fun onError(throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
    }
    private fun showAddNoteDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogLayoutBinding = DialogAddDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog = builder.create()
        with(dialogLayoutBinding) {
            buttonAdd.setOnClickListener {
                dialog.dismiss()
                val currentGlucoseLevel = editGlucoseLevel.text.toString().toDouble()
                uploadDiabetesNoteDataToServer(
                    currentGlucoseLevel,
                    "19:00",
                    "24.06.21"
                )
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
    private fun uploadDiabetesNoteDataToServer(
        glucoseLevel: Double,
        noteTime: String,
        noteDate: String
    ) {
        val response = getHashcodeFromPreferences()?.let {
            RequestAddDiabetesNote(
                it,
                glucoseLevel,
                noteTime,
                noteDate
            )
        }?.let {
            networkService?.addNote(
                it
            )
        }
        response?.enqueue(object : Callback<DiabetesNote> {
            override fun onResponse(
                call: Call<DiabetesNote>,
                response: Response<DiabetesNote>
            ) {
                val diabetesNote: DiabetesNote? = response.body()
                diabetesNote?.let { addDiabetesNoteToList(it) }
            }
            override fun onFailure(
                call: Call<DiabetesNote>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    private fun addDiabetesNoteToList(diabetesNote: DiabetesNote) {
        adapter?.addNote(diabetesNote)
        binding.recyclerView.adapter = adapter
    }
    private fun showEditNoteDialog(diabetesNote: DiabetesNote) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogEditDiabetesNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            editGlucoseLevel.setText(diabetesNote.glucoseLevel.toString())
            buttonSave.setOnClickListener {
                dialog?.dismiss()
                updateDiabetesNoteOnServer(
                    DiabetesNote(
                        diabetesNote.id,
                        0,
                        editGlucoseLevel.text.toString().toDouble(),
                        "1i:00",
                        "24.06.21"
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
    private fun updateDiabetesNoteOnServer(
        diabetesNote: DiabetesNote
    ) {
        val response = context?.let {
            getHashcodeFromPreferences()?.let { it1 ->
                RequestUpdateDiabetesNote(
                    it1,
                    diabetesNote.glucoseLevel,
                    diabetesNote.noteTime,
                    diabetesNote.noteDate
                )
            }?.let { it2 ->
                networkService?.updateNote(
                    diabetesNote.id,
                    it2
                )
            }
        }
        response?.enqueue(object : Callback<DiabetesNote> {
            override fun onResponse(
                call: Call<DiabetesNote>,
                response: Response<DiabetesNote>
            ) {
                response.body()?.let { note->
                    updateDiabetesNoteInList(
                        note
                    )
                }
            }
            override fun onFailure(
                call: Call<DiabetesNote>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    fun updateDiabetesNoteInList(diabetesNote: DiabetesNote) {
        adapter?.updateNote(diabetesNote)
        binding.recyclerView.adapter = adapter
    }
    private fun updateListOfDiabetesNotes(listOfDiabetesNotes: ArrayList<DiabetesNote>) {
        adapter?.updateList(listOfDiabetesNotes)
        binding.recyclerView.adapter = adapter
        binding.spinnerSort.onItemSelectedListener = this@FragmentDiabetesDiary
    }
    private fun showRemoveNoteDialog(noteId: Int) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialogLayoutBinding = DialogRemoveNoteBinding.inflate(LayoutInflater.from(context))
        val dialog: AlertDialog? = builder?.create()
        with(dialogLayoutBinding) {
            buttonRemoveNote.setOnClickListener {
                dialog?.dismiss()
                removeDiabetesNoteFromServer(noteId)
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
    private fun removeDiabetesNoteFromServer(noteId: Int) {
        val response = context?.let {
            getHashcodeFromPreferences()?.let { it1 ->
                networkService?.deleteNote(
                    noteId,
                    it1
                )
            }
        }
        response?.enqueue(object : Callback<Int> {
            override fun onResponse(
                call: Call<Int>,
                response: Response<Int>
            ) {
                val responseNoteId: Int? = response.body()
                responseNoteId?.let { removeDiabetesNoteFromList(it) }
            }
            override fun onFailure(
                call: Call<Int>,
                throwable: Throwable
            ) {
                throwable.printStackTrace()
            }
        })
    }
    private fun removeDiabetesNoteFromList(position: Int) {
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
                .withHeader(
                    "NoteId",
                    "UserId",
                    "GlucoseLevel",
                    "NoteTime",
                    "NoteDate"
                )
                .parse(
                    reader
                )
        
            for (record in records) {
                list.add(
                    DiabetesNote(
                        record[0].toInt(),
                        record[1].toInt(),
                        record[2].toDouble(),
                        record[3],
                        record[4]
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
                adapter?.getNotes()?.let { diabetesNotes ->
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
                adapter?.getNotes()?.let { diabetesNotes ->
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
                adapter?.getNotes()?.let { diabetesNotes ->
                    handleSaveToDirectory(
                        editFilename.text.toString() + extensionName,
                        pathToFile,
                        currentExtension,
                        diabetesNotes
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
    private fun tryToDoBackgroundSerializationAndSavingToFile(
        backgroundTaskListener: (String, ArrayList<DiabetesNote>) -> Unit,
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        Single
            .fromCallable {
                backgroundTaskListener(
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
    private fun tryToSerializeCsvAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<DiabetesNote> ->
                serializeCSV(
                    filePath,
                    notes
                )
            },
            pathToFile,
            diabetesNotes
        )
    }
    private fun tryToSerializeXmlAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<DiabetesNote> ->
                serializeXML(
                    filePath,
                    notes
                )
            },
            pathToFile,
            diabetesNotes
        )
    }
    private fun tryToSerializeJsonAndSaveToFile(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        tryToDoBackgroundSerializationAndSavingToFile(
            { filePath: String, notes: ArrayList<DiabetesNote> ->
                serializeJSON(
                    filePath,
                    notes
                )
            },
            pathToFile,
            diabetesNotes
        )
    }
    private fun serializeCSV(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val writer = Files.newBufferedWriter(Paths.get(pathToFile))
        val csvPrinter = CSVPrinter(
            writer,
            CSVFormat.DEFAULT.withHeader(
                "NoteId",
                "UserId",
                "GlucoseLevel",
                "NoteTime",
                "NoteDate"
            )
        )
        for (note in diabetesNotes) {
            val data = listOf(
                note.id,
                note.userId,
                note.glucoseLevel,
                note.noteTime,
                note.noteDate
            )
            csvPrinter.printRecord(data)
        }
        csvPrinter.flush()
        csvPrinter.close()
    }
    private fun serializeXML(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val serializedString = XStream().toXML(diabetesNotes)
        context?.openFileOutput(pathToFile, Context.MODE_PRIVATE).use {
            it?.write(serializedString.toByteArray())
        }
    }
    private fun serializeJSON(
        pathToFile: String,
        diabetesNotes: ArrayList<DiabetesNote>
    ) {
        val serializedString = Json.encodeToString(diabetesNotes)
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