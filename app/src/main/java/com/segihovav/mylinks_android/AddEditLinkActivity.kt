package com.segihovav.mylinks_android

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

class AddEditLinkActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private var myLinkItem: MyLink? = null
     private var isAdding: Boolean = false
     private lateinit var Name: TextInputLayout
     private lateinit var URL: TextInputLayout
     private lateinit var typeIDSpinner: Spinner
     private lateinit var builder: androidx.appcompat.app.AlertDialog.Builder

     override fun onCreate(savedInstanceState: Bundle?) {
          DataService.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
          this.setTheme(if (DataService.sharedPreferences.getBoolean("DarkThemeOn", false)) DataService.darkMode else DataService.lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.addeditlinkactivity)

          builder = androidx.appcompat.app.AlertDialog.Builder(this)

          val titleBar=findViewById<TextView>(R.id.TitleBar)

          Name=findViewById(R.id.Name)

          URL=findViewById(R.id.URL)

          typeIDSpinner = findViewById(R.id.TypeID)

          // Spinner click listener
          typeIDSpinner.onItemSelectedListener = this

          // Get Intent data
          val extras = intent.extras

          if (extras != null) {
               // Get IsAdding extra
               try {
                    if (extras.getBoolean(applicationContext.packageName + ".IsAdding")) {
                         isAdding = true

                         titleBar.text = "New link"
                    }
               } catch (e: Exception) {
                    isAdding = false
               }

               // remove "All" link type
               DataService.myLinksTypeNames.remove("All")

               // When adding a link,
               if (isAdding)
                    DataService.myLinksTypeNames.add(0,"")

               // Creating adapter for spinner - For some reason when adding, we need to use android.R.layout.simple_spinner_item when adding or the TypeID spinner items will have too large of a gap between each item
               val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DataService.myLinksTypeNames)

               // attaching data adapter to spinner
               typeIDSpinner.adapter = dataAdapter

               // Existing item
               if (!isAdding) {
                    myLinkItem=extras.getParcelable<MyLink>(applicationContext.packageName + ".LinkItem") // Get Link Item

                    // set the title
                    titleBar.text = "${DataService.getActiveInstanceDisplayName()} # ${myLinkItem?.ID}"

                    Name.editText?.setText(myLinkItem?.Name)

                    URL.editText?.setText(myLinkItem?.URL)

                    for (i in DataService.myLinksTypes.indices) {
                         if (DataService.myLinksTypes[i].ID == myLinkItem?.TypeID) {
                              for (j in DataService.myLinksTypeNames.indices) {
                                   if (DataService.myLinksTypeNames[j] == DataService.myLinksTypes[i].Name) {
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

     fun goBackClick(v: View) {
          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }

     private fun processData(getLinkDataEndpoint: String, params: String) {
          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(
               Request.Method.GET, DataService.MyLinksActiveURL + getLinkDataEndpoint + params, null,
               { _ ->
               },
               {
                    DataService.alert(builder, message="An error occurred " + if(!isAdding) "saving" else "adding" + " the link with the error " + it.toString(),finish={ finish() }, OKCallback=null)
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
               DataService.alert(builder, message="Please enter the name",finish={ finish() },OKCallback = null)
              return
          }

          if (url != null && url.isEmpty()) {
               DataService.alert(builder, message="Please enter the URL",finish={ finish() }, OKCallback=null)
               return
          }

          if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
               DataService.alert(builder, message="The URL that you entered is not valid",finish={ finish() }, OKCallback=null)
               return
          }

          if (typeIDSpinner.selectedItem == "") {
               DataService.alert(builder, message="Please select the type",finish={ finish() }, OKCallback=null)
               return
          }

          // Save existing item
          if (!isAdding) {
               getLinkDataEndpoint = "LinkData.php?task=updateRow"

               params="&rowID=${this.myLinkItem?.ID}&columnName=Name&columnValue=${name}"
               processData(getLinkDataEndpoint, params)

               params="&rowID=${this.myLinkItem?.ID}&columnName=URL&columnValue=${url}"
               processData(getLinkDataEndpoint, params)

               for (i in DataService.myLinksTypes.indices) {
                    val linkTypeName=if (DataService.myLinksTypes[i].Name != null) DataService.myLinksTypes[i].Name else ""

                    if (linkTypeName == typeIDSpinner.selectedItem) {
                         params="&rowID=${this.myLinkItem?.ID}&columnName=TypeID&columnValue=${DataService.myLinksTypes[i].ID}"
                         processData(getLinkDataEndpoint, params)
                    }
               }
          } else { // Save new item
               getLinkDataEndpoint = "LinkData.php?task=insertRow"

               for (i in DataService.myLinksTypes.indices) {
                    val linkTypeName=if (DataService.myLinksTypes[i].Name != null) DataService.myLinksTypes[i].Name else ""

                    if (linkTypeName == typeIDSpinner.selectedItem) {
                         //params="&rowID=${this.myLinkItem?.ID}&columnName=TypeID&columnValue=${DataService.myLinksTypes[i].ID}"
                         //processData(getLinkDataEndpoint, params)
                    }
               }

               params="&Name=${name}&URL=${url}&Type=${typeIDSpinner.selectedItem}"

               processData(getLinkDataEndpoint, params)
          }

          val intent = Intent(this, MainActivity::class.java)
          startActivity(intent)
     }
}
