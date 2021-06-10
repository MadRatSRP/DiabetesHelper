package com.madrat.diabeteshelper.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.madrat.diabeteshelper.databinding.FragmentProductBinding
import com.madrat.diabeteshelper.databinding.FragmentUserBinding
import com.madrat.diabeteshelper.linearManager
import com.madrat.diabeteshelper.network.NetworkClient
import com.madrat.diabeteshelper.ui.fooddiary.FoodNotesAdapter
import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote
import com.madrat.diabeteshelper.ui.user.UserNetworkInterface
import com.madrat.diabeteshelper.ui.user.model.RequestRegisterUser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FragmentProduct: Fragment() {
    private var nullableBinding: FragmentProductBinding? = null
    private val binding get() = nullableBinding!!
    private var adapter: ProductAdapter? = null
    private var networkService: ProductNetworkInterface? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        nullableBinding = FragmentProductBinding.inflate(
            inflater,
            container,
            false
        )
        adapter = ProductAdapter()
        val view = binding.root
        networkService = context?.let {
            NetworkClient
                .getRetrofit(it)
                .create(ProductNetworkInterface::class.java)
        }
        with(binding) {
            recyclerView.adapter = adapter
            recyclerView.linearManager()
        }
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProducts()
    }
    
    private fun getProducts() {
        val foodNotesResponse = context?.let {
            networkService?.getProducts()?.apply {
                subscribeOn(Schedulers.io())
                observeOn(AndroidSchedulers.mainThread())
            }
        }
        foodNotesResponse?.subscribeWith(object : DisposableSingleObserver<ArrayList<Product>>() {
            override fun onSuccess(list: ArrayList<Product>?) {
                activity?.runOnUiThread {
                    list?.let { updateListOfProducts(it) }
                }
            }
            override fun onError(throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
    }
    private fun updateListOfProducts(listOfProducts: ArrayList<Product>) {
        adapter?.updateList(listOfProducts)
        binding.recyclerView.adapter = adapter
    }
}