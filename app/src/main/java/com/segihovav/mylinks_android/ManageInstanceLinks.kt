package com.segihovav.mylinks_android

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ManageInstanceLinks : AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private var recyclerviewAdapterInstance: ManageInstanceLinksRecyclerviewAdapter? = null
     private lateinit var manageLinksRecyclerListView: RecyclerView
     private lateinit var addMyLinksName: TextInputLayout
     private lateinit var addMyLinksURL: TextInputLayout
     private lateinit var touchListener: RecyclerTouchListener

     override fun onCreate(savedInstanceState: Bundle?) {
          val layoutManager: RecyclerView.LayoutManager

          super.onCreate(savedInstanceState)
          setContentView(R.layout.manage_instance_links)

          addMyLinksName = findViewById(R.id.AddName)
          addMyLinksURL = findViewById(R.id.AddURL)

          manageLinksRecyclerListView = findViewById(R.id.URLList)

          recyclerviewAdapterInstance = ManageInstanceLinksRecyclerviewAdapter(this, DataService.myLinkInstanceURLSNames)

          layoutManager = LinearLayoutManager(applicationContext)

          manageLinksRecyclerListView.layoutManager = layoutManager
          manageLinksRecyclerListView.itemAnimator = DefaultItemAnimator()

          //registerForContextMenu(manageLinksRecyclerListView)

          manageLinksRecyclerListView.adapter = recyclerviewAdapterInstance

          val builder= AlertDialog.Builder(this)

          touchListener = RecyclerTouchListener(this, manageLinksRecyclerListView)
          touchListener
               .setClickable(object : RecyclerTouchListener.OnRowClickListener {
                    override fun onRowClicked(position: Int) { /* Toast.makeText(applicationContext, myLinksList.get(position).Name, Toast.LENGTH_SHORT).show() */ }

                    override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
                })
               .setSwipeOptionViews(R.id.delete_link)
               .setSwipeable(R.id.rowFG, R.id.rowBG, object : RecyclerTouchListener.OnSwipeOptionsClickListener {
                    override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                        when (viewID) {
                            R.id.delete_link -> DataService.alert(builder, message="Are you sure that you want to delete this link ?", confirmDialog=true, finish={ finish() }) { deleteRow((position)) }
                        }
                    }
                })

          manageLinksRecyclerListView.addOnItemTouchListener(touchListener)
     }

     override fun onConfigurationChanged(newConfig: Configuration) {
          super.onConfigurationChanged(newConfig)

          //if (manageLinksRecyclerListView.adapter != null)
          //     manageLinksRecyclerListView.adapter?.notifyDataSetChanged()
     }

     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { }

     override fun onNothingSelected(p0: AdapterView<*>) { }

     fun addURLClick(v: View) {
         // Validate name and URL fields
         if (addMyLinksName.editText != null && addMyLinksName.editText?.text != null && addMyLinksName.editText?.text.toString() == "") {
               Toast.makeText(applicationContext, "Please enter the Name", Toast.LENGTH_LONG).show()
               return
          }

          if (addMyLinksURL.editText != null && addMyLinksURL.editText?.text != null && addMyLinksURL.editText?.text.toString() == "") {
               Toast.makeText(applicationContext, "Please enter the URL", Toast.LENGTH_LONG).show()
               return
          }

          // Make sure that this URL has not been added before
          for (i in DataService.instanceURLType.indices) {
               if (DataService.instanceURLType[i].URL == addMyLinksURL.editText?.text.toString()) {
                 Toast.makeText(applicationContext, "This URL has already been added enter the URL", Toast.LENGTH_LONG).show()
                 return
               }
          }

          /// Add to list used for spinner
          DataService.myLinkInstanceURLSNames.add(addMyLinksName.editText?.text.toString())

          recyclerviewAdapterInstance?.notifyDataSetChanged()

          DataService.instanceURLType.add(InstanceURLType(addMyLinksName.editText?.text.toString(),addMyLinksURL.editText?.text.toString()))
          // Add to Firebase
          if (DataService.useFirebase) {
              val database = FirebaseDatabase.getInstance()
              var myRef = database.getReference("MyLinks/" + addMyLinksName.editText?.text.toString())
              myRef.setValue(addMyLinksURL.editText?.text.toString());
          } else {
              val requestQueue: RequestQueue = Volley.newRequestQueue(this)

              val request = JsonArrayRequest(Request.Method.GET, DataService.JSONBaseURL +  "?MyLinks-Instances-Auth=" + DataService.JSONAuthToken + "&task=addURL&Name=" + addMyLinksName.editText?.text.toString() + "&URL=" + addMyLinksURL.editText?.text.toString(), null,
                      { _ ->
                      },
                      {
                          //DataService.alert(builder= AlertDialog.Builder(this), message="An error occurred adding an Instance URL with the error $it. Please check your network connection", finish={ finish() }, OKCallback=null)
                      }
              )
              requestQueue.add(request)
          }

          // Clear name and URL fields
          addMyLinksName.editText?.setText("")
          addMyLinksURL.editText?.setText("")
    }

     fun deleteRow(deletingItemIndex: Int = -1) {
          if (deletingItemIndex == -1) {
              DataService.alert(builder=AlertDialog.Builder(this), message="deletingItemIndex was not provided", finish={ finish() }, OKCallback=null)
              return
          }

          // Delete from Firebase
          if (DataService.useFirebase) {
              val database = FirebaseDatabase.getInstance()
              var myRef = database.getReference("MyLinks/" + DataService.instanceURLType[deletingItemIndex].Name)
              myRef.removeValue()
          } else {
              val requestQueue: RequestQueue = Volley.newRequestQueue(this)

              val request = JsonArrayRequest(Request.Method.GET, DataService.JSONBaseURL +  "?MyLinks-Instances-Auth=" + DataService.JSONAuthToken + "&task=deleteURL&Name=" + DataService.instanceURLType[deletingItemIndex].Name, null,
                      { _ ->
                      },
                      {
                          //DataService.alert(builder= AlertDialog.Builder(this), message="An error occurred adding an Instance URL with the error $it. Please check your network connection", finish={ finish() }, OKCallback=null)
                      }
              )
              requestQueue.add(request)
          }

          DataService.instanceURLType.removeAt(deletingItemIndex)
          DataService.myLinkInstanceURLSNames.removeAt(deletingItemIndex)

          recyclerviewAdapterInstance?.notifyDataSetChanged()

          DataService.alert(builder=AlertDialog.Builder(this), message="Item has been deleted", finish={ finish() }, OKCallback=null)
     }

     fun goBackClick(v: View?) {
          if (DataService.instanceURLType.size == 0) {
               // If there are no MyLink instance URLs unset active URL
               val editor = DataService.sharedPreferences.edit()
               editor.putString("MyLinksActiveURL","")
               editor.apply()

               DataService.alert(builder=AlertDialog.Builder(this), message="Please add at least 1 MyLinks instance URL", finish={ finish() }, OKCallback=null)
               return
          }

          val intent = Intent(this, SettingsActivity::class.java)
          startActivity(intent)
     }
}
