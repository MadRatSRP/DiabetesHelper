package com.madrat.diabeteshelper.logic

import android.os.Parcelable
import com.opencsv.bean.CsvBindByName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Home(
    val author: String,

    val value: String
): Parcelable