package com.segihovav.abalinks_android

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout

class EditActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private var abaLinkItem: AbaLink? = null
     private var isAdding: Boolean = false
     private lateinit var Name: TextInputLayout
     private lateinit var URL: TextInputLayout
     private lateinit var typeIDSpinner: Spinner

     override fun onCreate(savedInstanceState: Bundle?) {
          DataService.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (DataService.sharedPreferences.getBoolean("DarkThemeOn", false)) DataService.darkMode else DataService.lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.editactivity)

          DataService.AbaLinksURL = if (DataService.sharedPreferences.getString("AbaLinksURL", "") != null) DataService.sharedPreferences.getString("AbaLinksURL", "").toString() else ""

          if (DataService.AbaLinksURL != "" && !DataService.AbaLinksURL.endsWith("/"))
               DataService.AbaLinksURL+="/"

          val titleBar=findViewById<TextView>(R.id.TitleBar)

          Name=findViewById(R.id.Name)

          URL=findViewById(R.id.URL)

          typeIDSpinner = findViewById(R.id.TypeID)

          // Spinner click listener
          typeIDSpinner.onItemSelectedListener = this

          // Get Intent data
          val extras = intent.extras

          if (extras != null) {
               try {
                    if (extras.getBoolean(applicationContext.packageName + ".IsAdding")) {
                         isAdding = true

                         titleBar.text = "New link"
                    }
               } catch (e: Exception) {
                    isAdding = false
               }

               //DataService.abaLinksTypes = extras.getParcelableArrayList<AbaLinkType>(applicationContext.packageName + ".LinkTypes") as ArrayList<AbaLinkType>

               //DataService.abaLinksTypeNames = extras.getStringArrayList(applicationContext.packageName + ".LinkTypeNames") as ArrayList<String>

               // remove "All" link type
               DataService.abaLinksTypeNames.remove("All")

               if (isAdding)
                    DataService.abaLinksTypeNames.add(0,"")

               // Creating adapter for spinner - For some reason when adding, we need to use android.R.layout.simple_spinner_item when adding or the TypeID spinner items will have too large of a gap between each item
               val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DataService.abaLinksTypeNames)

               // attaching data adapter to spinner
               typeIDSpinner.adapter = dataAdapter

               if (!isAdding) {
                    // Get Aba Link Item
                    abaLinkItem=extras.getParcelable<AbaLink>(applicationContext.packageName + ".LinkItem")

                    titleBar.text = "AbaLink # ${abaLinkItem?.ID}"

                   Name.editText?.setText(abaLinkItem?.Name)

                   URL.editText?.setText(abaLinkItem?.URL)

                   for (i in DataService.abaLinksTypes.indices) {
                       if (DataService.abaLinksTypes[i].ID == abaLinkItem?.TypeID) {
                           for (j in DataService.abaLinksTypeNames.indices) {
                                if (DataService.abaLinksTypeNames[j] == DataService.abaLinksTypes[i].Name) {
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

     private fun alert(message: String, closeApp: Boolean = false) {
          // Display dialog
          val builder = AlertDialog.Builder(this)
          builder.setMessage(message).setCancelable(false)
                 .setPositiveButton("OK") { _, _ -> if (closeApp) finish() }

          val alert = builder.create()

          alert.show()
     }

     fun goBackClick(v: View) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     private fun processData(getLinkDataEndpoint: String, params: String) {
          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(
          Request.Method.GET, DataService.AbaLinksURL + getLinkDataEndpoint + params, null,
          { _ ->
          },
          {
               //System.out.println("****** Error response=" + error.toString());
               alert("An error occurred " + if(!isAdding) "saving" else "adding" + " the link with the error " + it.toString(), false)
          })

          requestQueue.add(request)
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

               params="&rowID=${this.abaLinkItem?.ID}&columnName=Name&columnValue=${name}"
               processData(getLinkDataEndpoint, params)

               params="&rowID=${this.abaLinkItem?.ID}&columnName=URL&columnValue=${url}"
               processData(getLinkDataEndpoint, params)

               for (i in DataService.abaLinksTypes.indices) {
                    val linkTypeName=if (DataService.abaLinksTypes[i].Name != null) DataService.abaLinksTypes[i].Name else ""

                    if (linkTypeName == typeIDSpinner.selectedItem) {
                         params="&rowID=${this.abaLinkItem?.ID}&columnName=TypeID&columnValue=${DataService.abaLinksTypes[i].ID}"
                         processData(getLinkDataEndpoint, params)
                    }
               }
          } else { // Save new item
               getLinkDataEndpoint = "LinkData.php?task=insertRow"

               params="&Name=${name}&URL=${url}&Type=${typeIDSpinner.selectedItem}"

               processData(getLinkDataEndpoint, params)
          }

          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }
}