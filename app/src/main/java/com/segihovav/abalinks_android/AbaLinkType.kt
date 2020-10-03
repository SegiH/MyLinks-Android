package com.segihovav.abalinks_android

import android.os.Parcel
import android.os.Parcelable

data class AbaLinkType(var ID: Int, var Name: String? = ""): Parcelable {
  constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()) { }

    companion object CREATOR : Parcelable.Creator<AbaLinkType> {
        override fun createFromParcel(parcel: Parcel): AbaLinkType {
            return AbaLinkType(parcel)
        }

        override fun newArray(size: Int): Array<AbaLinkType?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
        return hashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ID)
        dest.writeString(Name)
    }
}
