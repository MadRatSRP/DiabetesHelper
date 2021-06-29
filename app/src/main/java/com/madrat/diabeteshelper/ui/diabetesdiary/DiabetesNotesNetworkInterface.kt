package com.madrat.diabeteshelper.ui.diabetesdiary

import com.madrat.diabeteshelper.ui.diabetesdiary.model.*
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DiabetesNotesNetworkInterface {
    @POST("diabetesNotes/addNote/")
    fun addNote(
        @Body requestAddDiabetesNote: RequestAddDiabetesNote
    ): Call<DiabetesNote>
    
    @GET("diabetesNotes/notes")
    fun getNotes(
        @Query("userHashcode") userHashcode: String
    ): Single<ArrayList<DiabetesNote>>
    
    @PUT("diabetesNotes/notes/{noteId}")
    fun updateNote(
        @Path("noteId") noteId: Int,
        @Body requestUpdateDiabetesNote: RequestUpdateDiabetesNote
    ): Call<DiabetesNote>
    
    @DELETE("diabetesNotes/notes/{noteId}")
    fun deleteNote(
        @Path("noteId") noteId: Int,
        @Query("userHashcode") userHashcode: String
    ): Call<Int>
}