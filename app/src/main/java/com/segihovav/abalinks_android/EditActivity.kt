package com.segihovav.abalinks_android

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class EditActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private lateinit var sharedPreferences: SharedPreferences
     private val darkMode = R.style.Theme_AppCompat_DayNight
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private val abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
     private val abaLinksTypeNames: ArrayList<String> = ArrayList()
     private lateinit var abaLinksURL: String
     private var isAdding: Boolean = false
     private var ID: Int = 0
     private lateinit var Name: TextInputLayout
     private lateinit var URL: TextInputLayout
     private lateinit var typeIDSpinner: Spinner
     private lateinit var deleteLink: ImageView

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.editactivity)

          abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != null) sharedPreferences.getString("AbaLinksURL", "").toString() else ""

          if (abaLinksURL != "" && !abaLinksURL.endsWith("/"))
               abaLinksURL+="/"

          val titleBar=findViewById<TextView>(R.id.TitleBar)

          Name=findViewById<TextInputLayout>(R.id.Name)

          URL=findViewById<TextInputLayout>(R.id.URL)

          typeIDSpinner = findViewById<Spinner>(R.id.TypeID)

          // Spinner click listener
          typeIDSpinner.onItemSelectedListener = this

          deleteLink = findViewById(R.id.deleteLink)

          var extras = intent.extras

          if (extras != null) {
               try {
                    if (extras.getBoolean("com.segihovav.abalinks_android.IsAdding") == true) {
                         isAdding = true

                         titleBar.text = "New link"

                         // Hide delete icon when adding a new link
                         deleteLink.visibility = View.GONE
                    }
               } catch (e: Exception) {
                    isAdding = false
               }

               var counter = 0

               while (extras.getString("com.segihovav.abalinks_android.LinkTypeID" + counter) != null) {
                    var IDKey="com.segihovav.abalinks_android.LinkTypeID" + counter

                    var linkTypeIDStr=extras.getString(IDKey)

                    var LinkTypeID :Int = 0

                    if (linkTypeIDStr != null && linkTypeIDStr != "")
                         LinkTypeID=linkTypeIDStr.toInt()

                    var LinkTypeName = extras.getString("com.segihovav.abalinks_android.LinkTypeName" + counter)

                    abaLinksTypes.add(AbaLinkType(LinkTypeID, LinkTypeName))

                    if (isAdding)
                        abaLinksTypeNames.add("")

                    if (LinkTypeName != null && LinkTypeName != "All")
                         abaLinksTypeNames.add(LinkTypeName)

                    counter++
               }

               // Creating adapter for spinner
               val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, abaLinksTypeNames)

               // attaching data adapter to spinner
               typeIDSpinner.adapter = dataAdapter

               if (!isAdding) {
                   val IDStr =  extras.getString("com.segihovav.abalinks_android.LinkID")

                    if (IDStr != null && IDStr != "")
                         ID=IDStr.toInt()

                   var LinkTypeIDstr=extras.getString("com.segihovav.abalinks_android.LinkTypeID")

                   var linkTypeID: Int = 0

                   if (LinkTypeIDstr != null && LinkTypeIDstr != "")
                      linkTypeID=LinkTypeIDstr.toInt()

                   var link=AbaLink(ID, extras.getString("com.segihovav.abalinks_android.LinkName"),extras.getString("com.segihovav.abalinks_android.LinkURL"), linkTypeID)

                    titleBar.text = "AbaLink # " + link.ID

                   Name.editText?.setText(link.Name)

                   URL.editText?.setText(link.URL)

                   for (i in abaLinksTypes.indices) {
                       if (abaLinksTypes[i].ID == link.TypeID) {
                           for (j in abaLinksTypeNames.indices) {
                                if (abaLinksTypeNames[j] == abaLinksTypes[i].Name) {
                                     typeIDSpinner.setSelection(j)
                                }
                           }
                       }
                   }
               }
          }
     }

     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { }

     override fun onNothingSelected(p0: AdapterView<*>?) { }

     private fun alert(message: String, closeApp: Boolean,confirmDialog: Boolean = false  ) {
          // Display dialog
          val builder = AlertDialog.Builder(this)

          builder.setMessage(message).setCancelable(confirmDialog)

          // When deleting, we need  yes/no cancelable buttons
          if (confirmDialog) {
               builder.setNegativeButton("Cancel") { _, _ -> if (closeApp) finish() }

               builder.setPositiveButton("OK") { _, _ ->
                    val getLinkDataEndpoint = "LinkData.php?task=deleteRow"

                    val params= "&LinkID=$ID"

                    processData(getLinkDataEndpoint, params)

                    // Go back to main activity after deleting
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    finish()
               }
          } else {
              builder.setPositiveButton("OK") { _, _ -> if (closeApp) finish() }
          }

          val alert = builder.create()

          alert.show()
     }

     fun deleteLinkClick(v: View) {
          alert("Are you sure that you want to delete this link ?",closeApp = false,confirmDialog = true)
     }

     fun goBackClick(v: View) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View) {
          var getLinkDataEndpoint: String
          var params: String

          val name=if (Name.editText?.text != null) Name.editText?.text else ""
          val url=if (URL.editText?.text != null) URL.editText?.text else ""

          // Validate all fields
          if (name != null && name.isEmpty()) {
              alert("Please enter the name", false)
              return
          }

          if (url != null && url.isEmpty()) {
               alert("Please enter the URL", false)
               return
          }

          if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
               alert("The URL that you entered is not valid", false)
               return
          }

          if (typeIDSpinner.selectedItem == "") {
               alert("Please select the type", false)
               return
          }

          // Save existing item
          if (!isAdding) {
               getLinkDataEndpoint = "LinkData.php?task=updateRow"

               params="&rowID=" + this.ID + "&columnName=Name&columnValue=" + name
               processData(getLinkDataEndpoint, params)

               params="&rowID=" + this.ID + "&columnName=URL&columnValue=" + url
               processData(getLinkDataEndpoint, params)

               for (i in abaLinksTypes.indices) {
                    val linkTypeName= if (abaLinksTypes[i].Name != null) abaLinksTypes[i].Name else ""

                    if (linkTypeName == typeIDSpinner.selectedItem) {
                         params="&rowID=" + this.ID + "&columnName=TypeID&columnValue=" + abaLinksTypes[i].ID
                         processData(getLinkDataEndpoint, params)
                    }
               }

          } else { // Save new item
               getLinkDataEndpoint = "LinkData.php?task=insertRow"

               params="&Name=" + name + "&URL=" + url + "&Type=" + typeIDSpinner.selectedItem

               processData(getLinkDataEndpoint, params)
          }

          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     private fun processData(getLinkDataEndpoint: String, params: String) {
          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(
                  Request.Method.GET,
                  abaLinksURL + getLinkDataEndpoint + params,
                  null,
                  Response.Listener { _ ->
                  },
                  Response.ErrorListener {
                       //System.out.println("****** Error response=" + error.toString());
                       //alert("An error occurred " + if(!isAdding) "saving" else "adding" + " the link with the error ", false)
                  })

          requestQueue.add(request)
     }
}
