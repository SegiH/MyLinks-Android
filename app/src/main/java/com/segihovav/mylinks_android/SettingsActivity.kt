package com.segihovav.mylinks_android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {
     private lateinit var myLinksURL: TextInputLayout
     private lateinit var darkModeCheckbox: SwitchMaterial
     private var darkModeToggled = false

     override fun onCreate(savedInstanceState: Bundle?) {
          DataService.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (DataService.sharedPreferences.getBoolean("DarkThemeOn", false)) DataService.darkMode else DataService.lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.settings)

          val titleBar=findViewById<TextView>(R.id.TitleBar)
          titleBar.setText(DataService.MyLinksTitle)

          darkModeCheckbox = findViewById(R.id.switchDarkMode)
          myLinksURL = findViewById(R.id.URL)

          if (DataService.sharedPreferences.getString("MyLinksURL", "") != "") {
               myLinksURL.editText?.setText(DataService.sharedPreferences.getString("MyLinksURL", ""))
          } else {
               DataService.alert(androidx.appcompat.app.AlertDialog.Builder(this), message="Please enter the URL",finish={ finish() },OKCallback = null)
          }

          darkModeCheckbox.isChecked = DataService.sharedPreferences.getBoolean("DarkThemeOn", false)
     }

     fun darkModeClick(v: View?) {
          darkModeToggled = true
          Toast.makeText(applicationContext, "The app will be close when you click on save for this to take effect" + if(darkModeCheckbox.isChecked) ". You must have Dark Mode enabled on Android " else "", Toast.LENGTH_SHORT).show()
     }

     fun goBackClick(v: View?) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View?) {
          if (myLinksURL.editText != null && myLinksURL.editText?.text != null && myLinksURL.editText?.text.toString() == "") {
               Toast.makeText(applicationContext, "Please enter the URL", Toast.LENGTH_LONG).show()
               return
          }
          val editor = DataService.sharedPreferences.edit()
          editor.putString("MyLinksURL", myLinksURL.editText?.text.toString())
          editor.putBoolean("DarkThemeOn", darkModeCheckbox.isChecked)
          editor.apply()

          if (darkModeToggled)
               finishAffinity()

          val intent = Intent(this, MainActivity::class.java)

          if (darkModeToggled)
               intent.putExtra(getApplicationContext().getPackageName() + ".DarkModeToggled", true)

          startActivity(intent)
     }
}