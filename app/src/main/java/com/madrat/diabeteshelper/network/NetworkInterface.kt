package com.madrat.diabeteshelper.network

import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.RequestAddDiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.ResponseAddDiabetesNote
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface NetworkInterface {
    // https://193.38.235.203:8443/dh_server/ diabetesNotes/notes
    
    @GET("diabetesNotes/notes")
    fun getDiabetesNotes()
        : Single<ArrayList<DiabetesNote>>
    
    @Headers("Content-Type: application/json")
    @POST("diabetesNotes/addNote")
    fun addDiabetesNote(
        @Body requestAddDiabetesNote: RequestAddDiabetesNote
    ): Call<ResponseAddDiabetesNote>
}