package com.segihovav.abalinks_android

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

class SettingsActivity : AppCompatActivity() {
     private lateinit var abaLinksURL: TextInputLayout
     private lateinit var sharedPreferences: SharedPreferences
     private lateinit var darkModeCheckbox: SwitchMaterial
     private val darkMode = R.style.ThemeOverlay_MaterialComponents_Dark
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private var darkModeToggled = false

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.settings)

          darkModeCheckbox = findViewById(R.id.switchDarkMode)
          abaLinksURL = findViewById(R.id.URL)

          if (sharedPreferences.getString("AbaLinksURL", "") != "") {
               abaLinksURL.editText?.setText(sharedPreferences.getString("AbaLinksURL", ""))
          } else {
               alert("Please enter the URL of your instance of AbaLinks", false)
          }

          darkModeCheckbox.isChecked = sharedPreferences.getBoolean("DarkThemeOn", false)
     }

     private fun alert(message: String, closeApp: Boolean) {
          // Display dialog
          val builder = AlertDialog.Builder(this)

          builder.setMessage(message).setCancelable(false)
                 .setPositiveButton("OK") { _, _ -> if (closeApp) finish() }

          val alert = builder.create()

          alert.show()
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
          if (abaLinksURL.editText != null && abaLinksURL.editText?.text != null && abaLinksURL.editText?.text.toString() == "") {
               Toast.makeText(applicationContext, "Please enter the URL", Toast.LENGTH_LONG).show()
               return
          }
          val editor = sharedPreferences.edit()
          editor.putString("AbaLinksURL", abaLinksURL.editText?.text.toString())
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