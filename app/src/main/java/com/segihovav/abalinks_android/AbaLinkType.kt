package com.segihovav.abalinks_android

import android.os.Parcel
import android.os.Parcelable

data class AbaLinkType(var ID: Int, var Name: String? = ""): Parcelable {
    var _ID: Int = 0
    lateinit var _Name: String

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) {
        _ID = parcel.readInt()
        _Name = parcel.readString().toString()
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ID)
        dest.writeString(Name)
    }

    //constructor used for parcel
    fun AbaLinkType(parcel: Parcel) {
        //read and set saved values from parcel
        ID=parcel.readInt()
        Name=parcel.readString().toString()
    }

    companion object CREATOR : Parcelable.Creator<AbaLinkType> {
        override fun createFromParcel(parcel: Parcel): AbaLinkType {
            return AbaLinkType(parcel)
        }

        override fun newArray(size: Int): Array<AbaLinkType?> {
            return arrayOfNulls(size)
        }
    }
}