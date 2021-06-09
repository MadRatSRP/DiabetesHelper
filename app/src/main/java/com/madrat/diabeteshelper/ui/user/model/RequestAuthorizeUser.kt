package com.madrat.diabeteshelper.ui.user.model

import kotlinx.serialization.Serializable

@Serializable
class RequestAuthorizeUser(
    val emailOrPhoneNumber: String,
    
    val password: String
)