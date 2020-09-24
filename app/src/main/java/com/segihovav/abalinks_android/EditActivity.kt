package com.segihovav.abalinks_android;

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import java.util.ArrayList


class EditActivity: AppCompatActivity() {
     private lateinit var sharedPreferences: SharedPreferences
     private val darkMode = R.style.Theme_AppCompat_DayNight
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private lateinit var episode: AbaLinks;
     private val abaLinksTypes: ArrayList<String> = ArrayList()

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.editactivity)

          val extras = intent.extras

          episode=AbaLinks(extras!!.getInt("EpisodeID",0),extras.getString("EpisodeName",""),extras.getString("EpisodeURL",""),extras.getInt("EpisodeTypeID",0))

          // continue here need to get types
          //abaLinksTypes=extras!!.getParcelableArrayList<String>("LinkTypes")

          val titleBar=findViewById<TextView>(R.id.TitleBar)

          titleBar.setText("AbaLink # " + episode.ID)
     }
}
