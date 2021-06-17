package com.madrat.diabeteshelper.ui.user

import com.madrat.diabeteshelper.ui.user.model.RequestAuthorizeUser
import com.madrat.diabeteshelper.ui.user.model.RequestRegisterUser
import com.madrat.diabeteshelper.ui.user.model.RequestUnauthorizeUser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserNetworkInterface {
    @POST("users/registerUser")
    fun registerUser(
        @Body requestRegisterUser: RequestRegisterUser
    ): Call<String>
    
    @POST("users/authorizeUser")
    fun authorizeUser(
        @Body requestAuthorizeUser: RequestAuthorizeUser
    ): Call<String>
    
    @POST("users/unauthorizeUser")
    fun unauthorizeUser(
        @Body requestUnauthorizeUser: RequestUnauthorizeUser
    ): Call<ResponseBody>
}