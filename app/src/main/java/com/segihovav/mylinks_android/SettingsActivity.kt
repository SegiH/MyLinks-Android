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
     private lateinit var darkModeCheckbox: SwitchMaterial
     private lateinit var myLinksURLs: Spinner
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

          myLinksURLs = findViewById<Spinner>(R.id.LinkURLSpinner)

          if (DataService.URLS.isNotEmpty()) {
               //if (DataService.URLS[0] != "")
                    //DataService.URLS.add(0,"")

               dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,DataService.URLS as List<String>)

               // attaching data adapter to spinner
               myLinksURLs.adapter = dataAdapter
          } else {
               dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mutableListOf())
          }

          if (DataService.sharedPreferences.getString("MyLinksActiveURL", "") != "") {
               for (i in DataService.URLS.indices) {
                    if (DataService.URLS[i] == DataService.sharedPreferences.getString("MyLinksActiveURL", ""))
                         myLinksURLs.setSelection(i)
               }
          } else {
               DataService.alert(androidx.appcompat.app.AlertDialog.Builder(this), message = "Please select the active MyLinks URL", finish = { finish() }, OKCallback = null)
          }

          darkModeCheckbox.isChecked = DataService.sharedPreferences.getBoolean("DarkThemeOn", false)
     }

     fun darkModeClick(v: View?) {
          darkModeToggled = true
          Toast.makeText(applicationContext, "The app will be close when you click on save for this to take effect" + if (darkModeCheckbox.isChecked) ". You must have Dark Mode enabled on Android " else "", Toast.LENGTH_SHORT).show()
     }

     fun goBackClick(v: View?) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun manageURLsClick(v: View?) {
          val intent = Intent(this, ManageLinks::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View?) {
          if (myLinksURLs.selectedItem == "") {
               Toast.makeText(applicationContext, "Please select the active MyLinks URL", Toast.LENGTH_LONG).show()
               return
           }

          val editor = DataService.sharedPreferences.edit()

          editor.putBoolean("DarkThemeOn", darkModeCheckbox.isChecked)
          editor.putString("MyLinksActiveURL",myLinksURLs.selectedItem.toString())
          editor.apply()

          DataService.MyLinksURL=myLinksURLs.selectedItem.toString()

          if (DataService.MyLinksURL.contains("ema"))
               DataService.MyLinksTitle="Ema Links"
          else if (DataService.MyLinksURL.contains("aba"))
               DataService.MyLinksTitle="Aba Links"

          if (darkModeToggled)
               finishAffinity()

          val intent = Intent(this, MainActivity::class.java)

          if (darkModeToggled)
               intent.putExtra(applicationContext.packageName + ".DarkModeToggled", true)

          startActivity(intent)
     }
}
