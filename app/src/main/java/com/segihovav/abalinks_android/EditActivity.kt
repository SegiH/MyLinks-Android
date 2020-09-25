package com.segihovav.abalinks_android;

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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
import java.lang.Exception
import java.util.*


class EditActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private lateinit var sharedPreferences: SharedPreferences
     private val darkMode = R.style.Theme_AppCompat_DayNight
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private var link: AbaLink? = null;
     private val abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
     private val abaLinksTypeNames: ArrayList<String> = ArrayList()
     private var abaLinksURL: String? = null
     private var isAdding: Boolean = false
     private lateinit var Name: TextInputLayout
     private lateinit var URL: TextInputLayout
     private lateinit var typeIDSpinner: Spinner

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.editactivity)

          abaLinksURL = if (sharedPreferences!!.getString("AbaLinksURL", "") != "") sharedPreferences!!.getString("AbaLinksURL", "") + (if (!sharedPreferences!!.getString("AbaLinksURL", "")!!.endsWith("/")) "/" else "") else ""


          val titleBar=findViewById<TextView>(R.id.TitleBar)

          Name=findViewById<TextInputLayout>(R.id.Name)

          URL=findViewById<TextInputLayout>(R.id.URL)

          typeIDSpinner = findViewById<Spinner>(R.id.TypeID)

          // Spinner click listener
          typeIDSpinner.setOnItemSelectedListener(this);

          // Creating adapter for spinner
          val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, abaLinksTypeNames)

          // attaching data adapter to spinner
          typeIDSpinner.setAdapter(dataAdapter);

          val extras = intent.extras

          if (extras != null) {
            try {
              if (extras!!.getBoolean("IsAdding") == true) {
                isAdding = true

                link = extras.getParcelable<AbaLink>("Link")!!

                titleBar.setText("New link")
              }
            } catch (e: Exception) {
              isAdding = false
            }

            if (!isAdding) {
              /*abaLinksTypes.clear()

              var counter = 0

              while (intent.extras!!.getString("LinkTypeID" + counter) != null) {
                var LinkTypeID = intent.extras!!.getString("LinkTypeID" + counter)!!.toInt()
                var LinkTypeName = intent.extras!!.getString("LinkTypeName" + counter)

                abaLinksTypes.add(AbaLinkType(LinkTypeID, LinkTypeName))

                if (LinkTypeName != null && !LinkTypeName.equals("All"))
                  abaLinksTypeNames.add(LinkTypeName)

                counter++
              }

            titleBar.setText("AbaLink # " + link.ID)

            Name.editText!!.setText(link.Name)

            URL.editText!!.setText(link.URL)

            for (i in abaLinksTypes.indices) {
              if (abaLinksTypes[i].ID == link.TypeID)
                typeIDSpinner.setSelection(i - 1)
            }*/
            }
          }
     }

     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
     }

     override fun onNothingSelected(p0: AdapterView<*>?) {
     }

     fun goBackClick(v: View?) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     fun saveClick(v: View?) {
          var getLinkDataEndpoint: String = ""
          var params: String = ""

          // Save existing item
          if (isAdding) {
               /*getLinkDataEndpoint = "LinkData.php?task=updateRow"

               params="&rowID=" + link.ID + "&columnName=Name&columnValue=" + Name.editText!!.text
               processData(getLinkDataEndpoint, params)

               params="&rowID=" + link.ID + "&columnName=URL&columnValue=" + URL.editText!!.text
               processData(getLinkDataEndpoint, params)

               var TypeID:Int =0

               for (i in abaLinksTypes.indices) {
                    if (abaLinksTypes[i].Name!!.equals(typeIDSpinner.selectedItem)) {
                         TypeID=abaLinksTypes[i].ID
                    }
               }

               params="&rowID=" + link.ID + "&columnName=TypeID&columnValue=" + TypeID

               processData(getLinkDataEndpoint, params)

                */
          } else { // Save new item
               //getLinkDataEndpoint = "LinkData.php?task=insertRow"

               //params="&Name=" +  java.net.URLEncoder.encode(link.Name, "utf-8") + "&URL=" + java.net.URLEncoder.encode(link.URL, "utf-8") + "&Type=" + link.TypeID
               //processData(getLinkDataEndpoint, params)
          }

          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     private fun processData(getLinkDataEndpoint: String,params: String) {
          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(
                  Request.Method.GET,
                  abaLinksURL + getLinkDataEndpoint + params,
                  null,
                  Response.Listener { response ->
                       var jsonarray: JSONArray? = null
                       try {
                            jsonarray = JSONArray(response.toString())
                       } catch (e: JSONException) {
                            e.printStackTrace()
                       }
                  },
                  Response.ErrorListener {
                       //System.out.println("****** Error response=" + error.toString());
                  })
          requestQueue.add(request)
     }
}
