package com.segihovav.mylinks_android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {
     private lateinit var addMyLinksURL: TextInputLayout
     private lateinit var darkModeCheckbox: SwitchMaterial
     private lateinit var myLinksURLs: Spinner
     private var URLS: MutableList<String?>? = mutableListOf()
     private lateinit var dataAdapter: ArrayAdapter<String>
     private var darkModeToggled = false

     override fun onCreate(savedInstanceState: Bundle?) {
          DataService.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (DataService.sharedPreferences.getBoolean("DarkThemeOn", false)) DataService.darkMode else DataService.lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.settings)

          val titleBar=findViewById<TextView>(R.id.TitleBar)
          titleBar.text = DataService.MyLinksTitle

          darkModeCheckbox = findViewById(R.id.switchDarkMode)
          addMyLinksURL = findViewById(R.id.AddURL)
          myLinksURLs = findViewById<Spinner>(R.id.LinkURLSpinner)

          if (DataService.sharedPreferences.getStringSet("MyLinksURLs", mutableSetOf())!!.isNotEmpty()) {
               URLS?.addAll(DataService.sharedPreferences.getStringSet("MyLinksURLs", mutableSetOf())!!)

               URLS?.add(0,"")

               dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,URLS as List<String>)

               // attaching data adapter to spinner
               myLinksURLs.adapter = dataAdapter
          } else {
               dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mutableListOf())
          }

          if (URLS != null && DataService.sharedPreferences.getString("MyLinksActiveURL", "") != "") {
               for (i in URLS!!.indices) {
                    if (URLS!![i] == DataService.sharedPreferences.getString("MyLinksActiveURL", ""))
                         myLinksURLs.setSelection(i)
               }
          } else {
               DataService.alert(androidx.appcompat.app.AlertDialog.Builder(this), message = "Please add a MyLinks URL", finish = { finish() }, OKCallback = null)
          }

          darkModeCheckbox.isChecked = DataService.sharedPreferences.getBoolean("DarkThemeOn", false)
     }

     fun addURLClick(v: View) {
          if (addMyLinksURL.editText != null && addMyLinksURL.editText?.text != null && addMyLinksURL.editText?.text.toString() == "") {
               Toast.makeText(applicationContext, "Please enter the URL", Toast.LENGTH_LONG).show()
               return
          }

          URLS?.add(addMyLinksURL.editText?.text.toString())

          dataAdapter.notifyDataSetChanged()

          if (URLS != null) {
            val editor = DataService.sharedPreferences.edit()
            editor.putStringSet("MyLinksURLs", URLS as MutableSet<String>)
            editor.apply()
          }
     }

     fun darkModeClick(v: View?) {
          darkModeToggled = true
          Toast.makeText(applicationContext, "The app will be close when you click on save for this to take effect" + if (darkModeCheckbox.isChecked) ". You must have Dark Mode enabled on Android " else "", Toast.LENGTH_SHORT).show()
     }

     fun goBackClick(v: View?) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View?) {
          if (myLinksURLs.selectedItem == "") {
               Toast.makeText(applicationContext, "Please select the active MyLinks URL", Toast.LENGTH_LONG).show()
               return
           }

          val editor = DataService.sharedPreferences.edit()

          editor.putString("MyLinksURL", addMyLinksURL.editText?.text.toString())
          editor.putBoolean("DarkThemeOn", darkModeCheckbox.isChecked)
          editor.putString("MyLinksActiveURL",myLinksURLs.selectedItem.toString())
          editor.apply()

          if (darkModeToggled)
               finishAffinity()

          val intent = Intent(this, MainActivity::class.java)

          if (darkModeToggled)
               intent.putExtra(applicationContext.packageName + ".DarkModeToggled", true)

          startActivity(intent)
     }
}
