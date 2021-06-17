package com.madrat.diabeteshelper.ui.fooddiary.model

import kotlinx.serialization.Serializable

@Serializable
class FoodNote(
    val id: Int,
    
    val userId: Int,
    
    val foodName: String,
)