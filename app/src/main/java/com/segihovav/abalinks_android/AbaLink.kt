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

    /*constructor(parcel: String?) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt()) {
        _ID = parcel.readInt()
        _Name = parcel.readString().toString()
        _URL = parcel.readString().toString()
        _TypeID = parcel.readInt()
    }*/

    //return hashcode of object
    /*override fun describeContents(): Int {
        return hashCode()
    }

    //write object values to parcel for storage
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        //write all properties to the parcle
        dest!!.writeInt(ID)
        dest.writeString(Name)
        dest.writeString(URL)
        dest.writeInt(TypeID)
    }*/

    //constructor used for parcel
    fun Property(parcel: Parcel?) {
        //read and set saved values from parcel
        ID=parcel!!.readInt()
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
    fun AbaLinkType(parcel: Parcel?) {
        //read and set saved values from parcel
        ID=parcel!!.readInt()
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