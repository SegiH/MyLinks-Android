package com.segihovav.mylinks_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue

abstract class SwipeControllerActions {
    // Edit
    fun onLeftClicked(link: List<MyLink>, position: Int, mainActivity: AppCompatActivity, linkTypes: ArrayList<MyLinkType>,linkTypeNames: ArrayList<String>) {
        val intent = Intent(mainActivity, EditActivity::class.java)

        intent.putExtra(MainActivity.context?.getPackageName() + ".LinkItem",link[position])
        intent.putExtra(MainActivity.context?.getPackageName() + ".LinkTypes",linkTypes)
        intent.putExtra(MainActivity.context?.getPackageName() + ".LinkTypeNames",linkTypeNames)

        mainActivity.startActivity(intent)
    }
}
