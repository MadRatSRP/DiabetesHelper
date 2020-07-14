package com.madrat.diabeteshelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.madrat.diabeteshelper.HomeMVP
import com.madrat.diabeteshelper.HomePresenter
import com.madrat.diabeteshelper.HomeRepository
import com.madrat.diabeteshelper.R

class HomeFragment: Fragment(), HomeMVP.View {
    private var presenter: HomePresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val floatingActionButton: FloatingActionButton = root.findViewById(R.id.fab)

        floatingActionButton.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMVP()

        // Инициализируем клиент Dropbox
        presenter?.initializeDropboxClient(context?.getString(R.string.dropbox_access_token)!!)

        presenter?.getDisplayNameDisposable()

        context?.let { presenter?.getMetadataDisposable(it, "Собачка села на травку и сказала гав-гав") }

        presenter?.getFileDisposable("/test.txt")
    }

    override fun setupMVP(){
        presenter = HomePresenter(this, HomeRepository())
    }

    override fun showDisplayName(displayName:String) {
        println(displayName)
    }
    /*override fun showFileMetadata(metadata: FileMetadata) {
        println(metadata.toStringMultiline())
    }*/
    override fun printFileContentToConsole(fileContent: String) {
        println(fileContent)
    }
}