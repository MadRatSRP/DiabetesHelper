package com.madrat.diabeteshelper.logic

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Home(
    val author: String,

    val value: String
): Parcelable