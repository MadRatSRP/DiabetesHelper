package com.madrat.diabeteshelper

import com.madrat.diabeteshelper.logic.Home

interface HomeMVP {
    interface View {

        fun showDisplayName(displayName: String)
        fun setupMVP()
        //fun showFileMetadata(metadata: FileMetadata)
        fun printFileContentToConsole(fileContent: String)
        fun updateListOfHomes(listOfHomes: ArrayList<Home>)
        fun showSaveAndExportDialog(view: android.view.View)
    }
    interface Presenter {

        /*fun getFileDisposable(filePath: String): @NonNull Disposable?
        fun getMetadataDisposable(context: Context, string: String): @NonNull Disposable?
        fun saveStringAsFile(context: Context, string: String)
        fun downloadFileFromServer(filePath: String): String*/
        fun setListOfHomes()
    }
    interface Repository {

    }
}