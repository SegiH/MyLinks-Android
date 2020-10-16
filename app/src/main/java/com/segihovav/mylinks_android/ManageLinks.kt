package com.segihovav.mylinks_android

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import org.json.JSONArray

class ManageLinks : AppCompatActivity(), AdapterView.OnItemSelectedListener {
     private var recyclerviewAdapter: ManageLinksRecyclerviewAdapter? = null
     private lateinit var manageLinksRecyclerListView: RecyclerView
     private lateinit var addMyLinksName: TextInputLayout
     private lateinit var addMyLinksURL: TextInputLayout
     private lateinit var touchListener: RecyclerTouchListener

     override fun onCreate(savedInstanceState: Bundle?) {
          val layoutManager: RecyclerView.LayoutManager

          super.onCreate(savedInstanceState)
          setContentView(R.layout.managelinks)

          addMyLinksName = findViewById(R.id.AddName)
          addMyLinksURL = findViewById(R.id.AddURL)

          manageLinksRecyclerListView = findViewById(R.id.URLList)

          recyclerviewAdapter = ManageLinksRecyclerviewAdapter(this, DataService.URLS)

          manageLinksRecyclerListView.setAdapter(recyclerviewAdapter)

          layoutManager = LinearLayoutManager(applicationContext)

          manageLinksRecyclerListView.layoutManager = layoutManager
          manageLinksRecyclerListView.itemAnimator = DefaultItemAnimator()

          registerForContextMenu(manageLinksRecyclerListView)

          manageLinksRecyclerListView.setAdapter(recyclerviewAdapter)

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

          if (manageLinksRecyclerListView.adapter != null)
               manageLinksRecyclerListView.adapter?.notifyDataSetChanged()
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
          for (i in DataService.URLS.indices) {
              if (DataService.URLS[i] == addMyLinksURL.editText?.text.toString()) {
                  Toast.makeText(applicationContext, "This URL has already been added enter the URL", Toast.LENGTH_LONG).show()
                  return
              }
          }

          // Add to list used for spinner
          DataService.URLS?.add(addMyLinksURL.editText?.text.toString())

          // Add to Firebase
          val database = FirebaseDatabase.getInstance()
          var myRef = database.getReference("MyLinks/" + addMyLinksName.editText?.text.toString())
          myRef.setValue(addMyLinksURL.editText?.text.toString());

          // Add to data store
          DataService.dataStore.add(FBDataStore(addMyLinksName.editText?.text.toString(),addMyLinksURL.editText?.text.toString()))

          recyclerviewAdapter?.notifyDataSetChanged()

          // Clear name and URL fields
          addMyLinksName.editText?.setText("")
          addMyLinksURL.editText?.setText("")
    }

     fun deleteRow(deletingItemIndex: Int = -1) {
          if (deletingItemIndex == -1) {
              DataService.alert(builder=AlertDialog.Builder(this), message="deletingItemIndex was not provided", finish={ finish() }, OKCallback=null)
              return
          }



          // Get the name of the item to delete'
         for (i in DataService.dataStore.indices) {
             if (DataService.dataStore[i].URL == DataService.URLS[deletingItemIndex]) {
                 val database = FirebaseDatabase.getInstance()
                 var myRef = database.getReference("MyLinks/" + DataService.dataStore[i].Name)
                 myRef.removeValue()

                 DataService.dataStore.removeAt(i)
             }
         }
          //val database = FirebaseDatabase.getInstance()
          //var myRef = database.getReference("MyLinks/" + addMyLinksName.editText?.text.toString())
          //myRef.removeValue()

          DataService.URLS.removeAt(deletingItemIndex)

          recyclerviewAdapter?.notifyDataSetChanged()

          DataService.alert(builder=AlertDialog.Builder(this), message="Item has been deleted", finish={ finish() }, OKCallback=null)
     }

     fun goBackClick(v: View?) {
          if (DataService.URLS.size == 0) {
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

     /*private fun saveURLS() {
          /*val editor = DataService.sharedPreferences.edit()
          editor.putStringSet("MyLinksURLs", DataService.URLS?.toHashSet())
          editor.apply()*/
    }*/
}