package com.madrat.diabeteshelper

import com.madrat.diabeteshelper.ui.diabetesdiary.DiabetesNote
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface NetworkInterface {
    // https://193.38.235.203:8443/dh_server/ diabetesNotes/notes
    
    @GET("diabetesNotes/notes")
    fun getDiabetesNotes()
        : Single<ArrayList<DiabetesNote>>
}