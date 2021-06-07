package com.madrat.diabeteshelper.ui.diabetesdiary

import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.RequestAddDiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.ResponseAddDiabetesNote
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.*

interface DiabetesNotesNetworkInterface {
    @GET("diabetesNotes/notes")
    fun getNotes()
        : Single<ArrayList<DiabetesNote>>
    
    @POST("diabetesNotes/addNote")
    fun addNote(
        @Body diabetesNote: DiabetesNote
    ): Call<DiabetesNote>
    
    @DELETE("diabetesNotes/notes/{noteId}")
    fun deleteNote(
        @Path("noteId") noteId: Int
    ): Call<Int>
    
    @PUT("diabetesNotes/notes/{noteId}")
    fun updateNote(
        @Path("noteId") noteId: Int,
        @Body diabetesNote: DiabetesNote
    ): Call<DiabetesNote>
}