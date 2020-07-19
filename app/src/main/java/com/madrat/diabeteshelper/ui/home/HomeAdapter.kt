package com.madrat.diabeteshelper.ui.home

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ListOfHomesBinding
import com.madrat.diabeteshelper.logic.util.inflate
import com.madrat.diabeteshelper.logic.Home
import kotlinx.android.extensions.LayoutContainer

class HomeAdapter
    : RecyclerView.Adapter<HomeAdapter.HomeHolder>() {
    private val listOfHomes = ArrayList<Home>()

    fun getListOfHomes()
            = listOfHomes

    fun updateListOfHomes(newListOfHomes: ArrayList<Home>) {
        listOfHomes.clear()
        listOfHomes.addAll(newListOfHomes)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder
            = HomeHolder(parent.inflate(R.layout.list_of_homes))

    override fun onBindViewHolder(holder: HomeHolder, position: Int)
            = holder.bind(listOfHomes[position])

    override fun getItemCount(): Int
            = listOfHomes.size

    inner class HomeHolder internal constructor(override val containerView: View)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private val binding = ListOfHomesBinding.bind(containerView);

        fun bind(home: Home) {
            binding.homeAuthor.text = home.author

            binding.homeValue.text = home.value
        }
    }
}