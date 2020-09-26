package com.segihovav.abalinks_android;

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import kotlin.properties.Delegates


class EditActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private lateinit var sharedPreferences: SharedPreferences
     private val darkMode = R.style.Theme_AppCompat_DayNight
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private lateinit var link: AbaLink
     private val abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
     private val abaLinksTypeNames: ArrayList<String> = ArrayList()
     private var abaLinksURL: String? = null
     private var isAdding: Boolean = false
     private lateinit var Name: TextInputLayout
     private lateinit var URL: TextInputLayout
     private lateinit var typeIDSpinner: Spinner
     private lateinit var deleteLink: ImageView

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.editactivity)

          abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != "") sharedPreferences.getString("AbaLinksURL", "") + (if (!sharedPreferences.getString("AbaLinksURL", "")!!.endsWith("/")) "/" else "") else ""

          val titleBar=findViewById<TextView>(R.id.TitleBar)

          Name=findViewById<TextInputLayout>(R.id.Name)

          URL=findViewById<TextInputLayout>(R.id.URL)

          typeIDSpinner = findViewById<Spinner>(R.id.TypeID)

          // Spinner click listener
          typeIDSpinner.setOnItemSelectedListener(this);

          deleteLink = findViewById(R.id.deleteLink)

          var extras = intent.extras

          if (extras != null) {
               try {
                    if (extras.getBoolean("com.segihovav.abalinks_android.IsAdding") == true) {
                         isAdding = true

                         titleBar.setText("New link")

                         // Hide delete icon when adding a new link
                         deleteLink.setVisibility(View.GONE);
                    }
               } catch (e: Exception) {
                    isAdding = false
               }

               var counter = 0

               while (extras.getString("com.segihovav.abalinks_android.LinkTypeID" + counter) != null) {
                    var IDKey="com.segihovav.abalinks_android.LinkTypeID" + counter

                    val LinkTypeID = if (extras.getString(IDKey) != null && !extras.getString(IDKey).equals("")) extras.getString(IDKey)!!.toInt() else 0

                    var LinkTypeName = extras.getString("com.segihovav.abalinks_android.LinkTypeName" + counter)

                    abaLinksTypes.add(AbaLinkType(LinkTypeID, LinkTypeName))

                    if (isAdding)
                        abaLinksTypeNames.add("")

                    if (LinkTypeName != null && (!LinkTypeName.equals("All")))
                         abaLinksTypeNames.add(LinkTypeName)

                    counter++
               }

               // Creating adapter for spinner
               val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, abaLinksTypeNames)

               // attaching data adapter to spinner
               typeIDSpinner.setAdapter(dataAdapter);

               if (!isAdding) {
                   var link=AbaLink(extras.getString("com.segihovav.abalinks_android.LinkID")!!.toInt(), extras.getString("com.segihovav.abalinks_android.LinkName"),extras.getString("com.segihovav.abalinks_android.LinkURL"), extras.getString("com.segihovav.abalinks_android.LinkTypeID")!!.toInt())

                   abaLinksTypes.clear()

                   titleBar.setText("AbaLink # " + link.ID)

                   Name.editText?.setText(link.Name)

                   URL.editText?.setText(link.URL)

                   for (i in abaLinksTypes.indices) {
                       if (abaLinksTypes[i].ID == link.TypeID)
                           typeIDSpinner.setSelection(i - 1)
                           break
                   }
               }
          }
     }

     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
     }

     override fun onNothingSelected(p0: AdapterView<*>?) {
     }

     private fun alert(message: String, closeApp: Boolean,confirmDialog: Boolean = false  ) {
          // Display dialog
          val builder = AlertDialog.Builder(this)
          builder.setMessage(message)
               .setCancelable(confirmDialog)
               .setPositiveButton("OK") { _, _ ->
                    // Fix me only do this when deleting
                    val getLinkDataEndpoint = "LinkData.php?task=deleteRow"

                    val params="&LinkID=" + link.ID

                    processData(getLinkDataEndpoint, params)

                    // Go back to main activity after deleting
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    finish()
               }

          if (confirmDialog)
               builder.setNegativeButton("Cancel") { _, _ -> if (closeApp) finish() }

          val alert = builder.create()

          alert.show()
     }

     fun deleteLinkClick(v: View) {
          alert("Are you sure that you want to delete this link ?",false,true)
     }

     fun goBackClick(v: View?) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View?) {
          var getLinkDataEndpoint: String = ""
          var params: String = ""

          // Validate all fields
          if (Name.editText!!.text.isEmpty()) {
              alert("Please enter the name", false)
              return
          }

          if (URL.editText!!.text.isEmpty()) {
               alert("Please enter the URL", false)
               return
          }

          if (!URL.editText!!.text.contains("http://") && !URL.editText!!.text.contains("https://")) {
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

               params="&rowID=" + link!!.ID + "&columnName=Name&columnValue=" + Name.editText!!.text
               processData(getLinkDataEndpoint, params)

               params="&rowID=" + link!!.ID + "&columnName=URL&columnValue=" + URL.editText!!.text
               processData(getLinkDataEndpoint, params)

               var TypeID:Int =0

               for (i in abaLinksTypes.indices) {
                    if (abaLinksTypes[i].Name!!.equals(typeIDSpinner.selectedItem)) {
                         TypeID=abaLinksTypes[i].ID
                    }
               }

               params="&rowID=" + link!!.ID + "&columnName=TypeID&columnValue=" + TypeID

               processData(getLinkDataEndpoint, params)
          } else { // Save new item
               getLinkDataEndpoint = "LinkData.php?task=insertRow"

               params="&Name=" +  java.net.URLEncoder.encode(Name.editText!!.text.toString(), "utf-8") + "&URL=" + java.net.URLEncoder.encode(URL.editText!!.text.toString(), "utf-8") + "&Type=" + typeIDSpinner.selectedItem

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
                  Response.Listener { response ->
                       //if (getLinkDataEndpoint.contains("deleteRow"))
                       //     return@Listener

                       var jsonarray: JSONArray? = null
                       try {
                            jsonarray = JSONArray(response.toString())
                       } catch (e: JSONException) {
                            e.printStackTrace()
                       }
                  },
                  Response.ErrorListener {
                       //System.out.println("****** Error response=" + error.toString());
                       //alert("An error occurred " + if(!isAdding) "saving" else "adding" + " the link with the error ", false)
                       // if (sharedPreferences!!.getString("AbaLinksURL", "") != "") sharedPreferences!!.getString("AbaLinksURL", "")
                  })

          requestQueue.add(request)
     }
}
