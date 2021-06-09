package com.madrat.diabeteshelper.ui.user

import com.madrat.diabeteshelper.ui.user.model.RequestRegisterUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserNetworkInterface {
    @POST("users/register")
    fun registerUser(
        @Body requestRegisterUser: RequestRegisterUser
    ): Call<Int>
    
    @POST("users/authorize")
    fun authorizeUser(
        @Body emailOrPhoneNumber: String,
        @Body password: String
    ): Call<User>
}