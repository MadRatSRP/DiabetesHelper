package com.madrat.diabeteshelper.ui.fooddiary

import com.madrat.diabeteshelper.ui.fooddiary.model.FoodNote
import com.madrat.diabeteshelper.ui.fooddiary.model.RequestUpdateFoodNote
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.*

interface FoodNotesNetworkInterface {
    @POST("foodNotes/addNote")
    fun addNote(
        @Body requestAddDiabetesNote: RequestAddFoodNote
    ): Call<FoodNote>
    
    @GET("foodNotes/notes")
    fun getNotes(
        @Query("userHashcode") userHashcode: String
    ): Single<ArrayList<FoodNote>>
    
    @PUT("foodNotes/notes/{noteId}")
    fun updateNote(
        @Path("noteId") noteId: Int,
        @Body requestUpdateDiabetesNote: RequestUpdateFoodNote
    ): Call<FoodNote>
    
    @DELETE("foodNotes/notes/{noteId}")
    fun deleteNote(
        @Path("noteId") noteId: Int,
        @Query("userHashcode") userHashcode: String
    ): Call<Int>
}