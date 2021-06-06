package com.madrat.diabeteshelper.network

import com.madrat.diabeteshelper.ui.diabetesdiary.model.DiabetesNote
import com.madrat.diabeteshelper.ui.diabetesdiary.model.RequestAddDiabetesNote
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NetworkInterface {
    // https://193.38.235.203:8443/dh_server/ diabetesNotes/notes
    
    @GET("diabetesNotes/notes")
    fun getDiabetesNotes()
        : Single<ArrayList<DiabetesNote>>
    
    @POST("diabetesNotes/addNote")
    fun addDiabetesNote(
        @Body sugarLevel: Double
    ): Call<DiabetesNote>
    
    /*
        @PostMapping(path="/addNote")
    public @ResponseBody String addNewNote(
            @RequestParam DiabetesNote note
    ) {
        diabetesNotesRepository.save(note);
        return "New note was saved";
    }
     */
}