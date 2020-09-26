package com.segihovav.abalinks_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue

abstract class SwipeControllerActions {
    private lateinit var requestQueue: RequestQueue

    // Edit
    fun onLeftClicked(link: List<AbaLink>, position: Int, mainActivity: AppCompatActivity, linkTypes: ArrayList<AbaLinkType>) {
        //mainActivity.intent = = Intent(this, EditActivity::class.java)
        val intent = Intent(mainActivity, EditActivity::class.java)

        // This throws a fatal parcelable error
        //intent.putExtra("com.segihovav.abalinks_android.Link",link[position])
        //intent.putExtra("com.segihovav.abalinks_android.LinkTypes",linkTypes)

        // If you don't cast INT to string, Kotlin throw a NULL pointer exception
        intent.putExtra("com.segihovav.abalinks_android.LinkID",link[position].ID.toString())
        intent.putExtra("com.segihovav.abalinks_android.LinkName",link[position].Name)
        intent.putExtra("com.segihovav.abalinks_android.LinkURL",link[position].URL)
        intent.putExtra("com.segihovav.abalinks_android.LinkTypeID",link[position].TypeID.toString())

        var counter=0

        // This seems to be the only way to copy the link types to another intent since I can't figure out how to use linkTypes as a value for putextra
        for (i in linkTypes.indices) {
            intent.putExtra("com.segihovav.abalinks_android.LinkTypeID" + counter,linkTypes[counter].ID.toString())
            intent.putExtra("com.segihovav.abalinks_android.LinkTypeName" + counter,linkTypes[counter].Name)
            counter++
        }

        mainActivity.startActivity(intent)
    }
}
