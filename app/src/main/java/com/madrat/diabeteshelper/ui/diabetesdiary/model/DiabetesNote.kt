package com.madrat.diabeteshelper.ui.diabetesdiary.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
class DiabetesNote(
    val id: Int,
    
    val sugarLevel: Double,
)

