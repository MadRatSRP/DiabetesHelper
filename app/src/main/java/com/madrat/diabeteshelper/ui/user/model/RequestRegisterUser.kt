package com.madrat.diabeteshelper.ui.user.model

import kotlinx.serialization.Serializable
import retrofit2.http.Body

@Serializable
class RequestRegisterUser(
    val emailOrPhoneNumber: String,
    
    val password: String
)