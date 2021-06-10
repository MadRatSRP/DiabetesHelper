package com.madrat.diabeteshelper.ui.product

import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface ProductNetworkInterface {
    @GET("products/")
    fun getProducts()
        : Single<ArrayList<Product>>
}