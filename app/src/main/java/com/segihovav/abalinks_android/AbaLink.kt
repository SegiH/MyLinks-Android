package com.segihovav.abalinks_android

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class AbaLink(var ID: Int, var Name: String?, var URL: String?, var TypeID: Int) : Parcelable {
    var _ID: Int = 0
    var _Name: String = ""
    var _URL: String =""
    var _TypeID: Int = 0
    var _abaLinksTypes: MutableList<AbaLinkType> = ArrayList()

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) { }

    companion object CREATOR : Parcelable.Creator<AbaLink> {
        override fun createFromParcel(parcel: Parcel): AbaLink {
            return AbaLink(parcel)
        }

        override fun newArray(size: Int): Array<AbaLink?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    //write object values to parcel for storage
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        //write all properties to the parcle
        dest!!.writeInt(ID)
        dest.writeString(Name)
        dest.writeString(URL)
        dest.writeInt(TypeID)
    }
}

