package com.segihovav.abalinks_android

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class AbaLink(var ID: Int, var Name: String?, var URL: String?, var TypeID: Int) { //: Parcelable {
    var _ID: Int = 0
    var _Name: String = ""
    var _URL: String =""
    var _TypeID: Int = 0
    var _abaLinksTypes: MutableList<AbaLinkType> = ArrayList()

    //constructor used for parcel
    fun Property(parcel: Parcel) {
        //read and set saved values from parcel
        ID=parcel.readInt()
        Name=parcel.readString().toString()
        URL=parcel.readString().toString()
        TypeID=parcel.readInt()
    }

    /*companion object CREATOR : Parcelable.Creator<AbaLink> {
        override fun createFromParcel(parcel: Parcel): AbaLink {
            return AbaLink(parcel)
        }

        override fun newArray(size: Int): Array<AbaLink?> {
            return arrayOfNulls(size)
        }
    }*/
}

