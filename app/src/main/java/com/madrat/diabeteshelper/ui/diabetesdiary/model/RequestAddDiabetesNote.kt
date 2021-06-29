package com.madrat.diabeteshelper.ui.diabetesdiary.model

import kotlinx.serialization.Serializable

class RequestAddDiabetesNote(
    val userHashcode: String,
    
    val glucoseLevel: Double,
    
    val noteTime: String,
    
    val noteDate: String
)