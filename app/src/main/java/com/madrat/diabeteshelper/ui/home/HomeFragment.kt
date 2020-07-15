package com.madrat.diabeteshelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.madrat.diabeteshelper.*
import com.madrat.diabeteshelper.databinding.FragmentHomeBinding
import com.madrat.diabeteshelper.logic.Home

class HomeFragment: Fragment(), HomeMVP.View {
    //private val binding by viewBinding(FragmentHomeBinding::bind)

    // ViewBinding variables
    private var mBinding: FragmentHomeBinding? = null
    private val binding get() = mBinding!!

    private var presenter: HomePresenter? = null

    private var adapter: HomeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // ViewBinding initialization
        mBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = HomeAdapter()

        binding.recyclerView.linearManager()
        binding.recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMVP()

        presenter?.setListOfHomes()

        // Инициализируем клиент Dropbox
        presenter?.initializeDropboxClient(context?.getString(R.string.dropbox_access_token)!!)

        presenter?.getDisplayNameDisposable()

        context?.let { presenter?.getMetadataDisposable(it, "Собачка села на травку и сказала гав-гав") }

        presenter?.getFileDisposable("/test.txt")

        binding.floatingActionButton.setOnClickListener { fabView ->
            Snackbar.make(fabView, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
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

    override fun updateListOfHomes(listOfHomes: ArrayList<Home>) {
        adapter?.updateListOfHomes(listOfHomes)
        binding.recyclerView.adapter = adapter
    }
}