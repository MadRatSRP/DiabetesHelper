package com.madrat.diabeteshelper

import com.dropbox.core.v2.files.FileMetadata
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.Disposable

interface HomeMVP {
    interface View {

        fun showDisplayName(displayName: String)
        fun setupMVP()
        //fun showFileMetadata(metadata: FileMetadata)
        fun printFileContentToConsole(fileContent: String)
    }
    interface Presenter {

        fun getFileDisposable(filePath: String): @NonNull Disposable?
    }
    interface Repository {

    }
}