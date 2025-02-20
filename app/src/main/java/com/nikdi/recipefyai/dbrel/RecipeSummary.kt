package com.nikdi.recipefyai.dbrel

import android.os.Parcelable
import android.os.Parcel

data class RecipeSummary(val id: String, val name: String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecipeSummary> {
        override fun createFromParcel(parcel: Parcel): RecipeSummary {
            return RecipeSummary(parcel)
        }

        override fun newArray(size: Int): Array<RecipeSummary?> {
            return arrayOfNulls(size)
        }
    }
}