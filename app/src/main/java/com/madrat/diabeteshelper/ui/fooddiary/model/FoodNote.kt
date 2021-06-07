package com.madrat.diabeteshelper.ui.fooddiary.model

import kotlinx.serialization.Serializable

@Serializable
class FoodNote(
    val noteId: Int,
    
    val foodName: String
)