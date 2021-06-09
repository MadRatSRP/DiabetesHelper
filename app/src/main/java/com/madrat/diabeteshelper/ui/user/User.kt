package com.madrat.diabeteshelper.ui.user

import kotlinx.serialization.Serializable

@Serializable
class User(
    val userId: Int,
    
    val emailOrPhoneNumber: String,
    
    val password: String,
    
    val userHashcode: Int,
    
    val isAuthorized: Boolean
)