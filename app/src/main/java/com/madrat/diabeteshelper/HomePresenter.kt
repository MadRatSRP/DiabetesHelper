package com.madrat.diabeteshelper

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream


class HomePresenter(private val view: HomeMVP.View,
                    private val repository: HomeMVP.Repository)
    : HomeMVP.Presenter  {

    private var client: DbxClientV2? = null

    fun getDisplayNameDisposable(accessToken: String): @NonNull Disposable? {
        return Observable.fromCallable {
            returnDisplayName(accessToken)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{result->
                view.showDisplayName(result)
            }
    }

    fun returnDisplayName(accessToken: String): String {
        val config = DbxRequestConfig
            .newBuilder("jopka")
            .build()
        client = DbxClientV2(config, accessToken)

        val fullAccount = client?.users()?.currentAccount

        return fullAccount?.name?.displayName!!
    }

    fun getMetadataDisposable(context: Context, string: String): @NonNull Disposable? {
        return Observable.fromCallable {
            saveStringAsFile(context, string)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{}
    }

    fun saveStringAsFile(context: Context, string: String) {
        val filePath = context.filesDir.path.toString() + "/fileName.txt"

        val file = File(filePath)

        file.writeText(string)

        // Upload "test.txt" to Dropbox
        FileInputStream(file).use { `in` ->
            client!!.files()
                .uploadBuilder("/DiabetesHelper/test.txt")
                .uploadAndFinish(`in`)
        }


    }

    override fun getFileDisposable(filePath: String): @NonNull Disposable? {
        return Observable.fromCallable {
            downloadFileFromServer(filePath)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{result ->
                view.printFileContentToConsole(result)
            }
    }

    fun downloadFileFromServer(filePath: String): String {
        return client!!.files().download(filePath)
            .inputStream
            .bufferedReader()
            .use(BufferedReader::readText)
    }
}