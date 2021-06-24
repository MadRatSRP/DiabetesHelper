package com.madrat.diabeteshelper.ui.diabetesdiary.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class DiabetesNote(
    val id: Int,
    
    val userId: Int,
    
    val glucoseLevel: Double,
    
    val noteTime: String,
    
    val noteDate: String
): Parcelable

