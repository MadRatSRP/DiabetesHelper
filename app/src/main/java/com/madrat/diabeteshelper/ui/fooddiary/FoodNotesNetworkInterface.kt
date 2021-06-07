package com.madrat.diabeteshelper.ui.fooddiary

import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.*

interface FoodNotesNetworkInterface {
    @GET("foodNotes/notes")
    fun getNotes()
        : Single<ArrayList<FoodNote>>
    
    @POST("foodNotes/addNote")
    fun addNote(
        @Body foodNote: FoodNote
    ): Call<FoodNote>
    
    @DELETE("foodNotes/notes/{noteId}")
    fun deleteNote(
        @Path("noteId") noteId: Int
    ): Call<Int>
    
    @PUT("foodNotes/notes/{noteId}")
    fun updateNote(
        @Path("noteId") noteId: Int,
        @Body foodNote: FoodNote
    ): Call<FoodNote>
}