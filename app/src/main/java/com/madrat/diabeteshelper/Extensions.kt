package com.madrat.diabeteshelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// RecyclerView
fun RecyclerView.linearManager() {
    this.layoutManager = LinearLayoutManager(context)
}

// Preferences
fun Fragment.getHashcodeFromPreferences(): String? {
    val preferences = activity?.getPreferences(Context.MODE_PRIVATE)
    
    return preferences?.getString(
        getString(R.string.key_user_hashcode),
        null
    )
}

fun AppCompatActivity.getHashcodeFromPreferences(): String? {
    val preferences = getPreferences(Context.MODE_PRIVATE)
    
    return preferences?.getString(
        getString(R.string.key_user_hashcode),
        null
    )
}

fun Fragment.saveHashcodeToPreferences(hashcode: String?) {
    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
    with(sharedPref.edit()) {
        putString(
            getString(R.string.key_user_hashcode),
            hashcode
        )
        commit()
    }
}

