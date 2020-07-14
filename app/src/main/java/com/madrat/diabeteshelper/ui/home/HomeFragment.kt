package com.madrat.diabeteshelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.madrat.diabeteshelper.HomeMVP
import com.madrat.diabeteshelper.HomePresenter
import com.madrat.diabeteshelper.HomeRepository
import com.madrat.diabeteshelper.R

class HomeFragment: Fragment(), HomeMVP.View {

    private lateinit var homeViewModel: HomeViewModel

    private var presenter: HomePresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
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