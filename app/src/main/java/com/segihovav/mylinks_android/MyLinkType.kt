package com.segihovav.mylinks_android

import android.os.Parcel
import android.os.Parcelable

data class MyLinkType(var ID: Int, var Name: String? = ""): Parcelable {
     constructor(parcel: Parcel) : this(
          parcel.readInt(),
          parcel.readString()) { }

     companion object CREATOR : Parcelable.Creator<MyLinkType> {
          override fun createFromParcel(parcel: Parcel): MyLinkType {
               return MyLinkType(parcel)
          }

          override fun newArray(size: Int): Array<MyLinkType?> {
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