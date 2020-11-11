package com.segihovav.mylinks_android

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONArray
import org.json.JSONException

class SettingsActivity : AppCompatActivity() {
     private lateinit var darkModeCheckbox: SwitchMaterial
     private lateinit var myLinksURLs: Spinner

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

          if (!DataService.useFirebase) {
               val requestQueue: RequestQueue = Volley.newRequestQueue(this)

               val request = JsonArrayRequest(Request.Method.GET, "https://mylinks-instances.hovav.org/?MyLinks-Instances-Auth=)j3s%3CltoEcv;eW=g0xX", null,
                       { response ->
                            var jsonarray: JSONArray = JSONArray()

                            try {
                                 jsonarray = JSONArray(response.toString())

                                 DataService.myLinkInstanceURLSNames.clear()
                                 DataService.dataStore.clear()

                                 for (i in 0 until jsonarray.length()) {
                                      try {
                                           val jsonobject = jsonarray.getJSONObject(i)

                                           DataService.dataStore.add(FirebaseDataStore(jsonobject.getString("Name"), jsonobject.getString("URL")))
                                           DataService.myLinkInstanceURLSNames.add(jsonobject.getString("Name"))
                                      } catch (e: JSONException) {
                                           //e.printStackTrace()
                                           DataService.alert(builder = AlertDialog.Builder(this), message = "An error occurred reading the links. Please check your network connection or the URL in Settings", finish = { finish() }, OKCallback = null)
                                      }
                                 }

                                 DataService.myLinksInstancesDataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,DataService.myLinkInstanceURLSNames as List<String>)

                                 // attaching data adapter to spinner
                                 myLinksURLs.adapter = DataService.myLinksInstancesDataAdapter

                                 if (DataService.sharedPreferences.getString("MyLinksActiveURL", "") != "") {
                                      for (i in DataService.dataStore.indices)
                                           if (DataService.dataStore[i].URL == DataService.sharedPreferences.getString("MyLinksActiveURL", ""))
                                                myLinksURLs.setSelection(i)
                                 } else {
                                      DataService.alert(androidx.appcompat.app.AlertDialog.Builder(this), message = "Please select the active MyLinks URL", finish = { finish() }, OKCallback = null)
                                 }
                            } catch (e: JSONException) {
                                 e.printStackTrace()
                            }
                       },
                       {
                            //DataService.alert(builder= AlertDialog.Builder(this), message="An error occurred reading the $dataType with the error $it. Please check your network connection", finish={ finish() }, OKCallback=null)
                       }
               )
               requestQueue.add(request)

               // For now, hide the Manage URLS button until I can finish the backend logic if we aren't using Firebase
               val addURLButton = findViewById<Button>(R.id.addURLButton)
               addURLButton.visibility = View.GONE
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

          for (i in DataService.dataStore.indices) {
               if (DataService.dataStore[i].Name == myLinksURLs.selectedItem) {
                    editor.putString("MyLinksActiveURL", DataService.dataStore[i].URL)
                    DataService.MyLinksActiveURL = DataService.dataStore[i].URL.toString()
                    break;
               }
          }

          editor.apply()

          if (DataService.MyLinksActiveURL.contains("ema"))
               DataService.MyLinksTitle="Ema Links"
          else if (DataService.MyLinksActiveURL.contains("aba"))
               DataService.MyLinksTitle="Aba Links"

          if (darkModeToggled)
               finishAffinity()

          val intent = Intent(this, MainActivity::class.java)

          if (darkModeToggled)
               intent.putExtra(applicationContext.packageName + ".DarkModeToggled", true)

          startActivity(intent)
     }
}
