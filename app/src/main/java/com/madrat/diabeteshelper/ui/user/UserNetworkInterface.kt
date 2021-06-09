package com.madrat.diabeteshelper.ui.user

import com.madrat.diabeteshelper.ui.user.model.RequestAuthorizeUser
import com.madrat.diabeteshelper.ui.user.model.RequestRegisterUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserNetworkInterface {
    @POST("users/registerUser")
    fun registerUser(
        @Body requestRegisterUser: RequestRegisterUser
    ): Call<Int>
    
    @POST("users/authorizeUser")
    fun authorizeUser(
        @Body requestAuthorizeUser: RequestAuthorizeUser
    ): Call<User>
}