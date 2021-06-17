package com.madrat.diabeteshelper.ui.diabetesdiary.model

import kotlinx.serialization.Serializable

@Serializable
class RequestAddDiabetesNote(
    val sugarLevel: Double,
    
    val userHashcode: String
)