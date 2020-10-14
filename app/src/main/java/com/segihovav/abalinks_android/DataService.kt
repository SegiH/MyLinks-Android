package com.segihovav.abalinks_android

import android.content.SharedPreferences
import java.util.ArrayList

class DataService {
    companion object {
        @JvmStatic lateinit var AbaLinksURL: String
        @JvmStatic lateinit var sharedPreferences: SharedPreferences
        @JvmStatic var abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
        @JvmStatic var abaLinksTypeNames: ArrayList<String> = ArrayList()
        @JvmStatic var lightMode = R.style.ThemeOverlay_MaterialComponents
        @JvmStatic var darkMode = R.style.ThemeOverlay_MaterialComponents_Dark
    }
}