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

        intent.putExtra("Link",link[position])

        var counter=0

        // This seems to be the only way to copy the link types to another intent since I can't figure out how to use linkTypes as a value for putextra
        for (i in linkTypes.indices) {
            intent.putExtra("LinkTypeID" + counter,linkTypes[counter].ID.toString())
            intent.putExtra("LinkTypeName" + counter,linkTypes[counter].Name)
            counter++
        }

        mainActivity.startActivity(intent)
    }
}
