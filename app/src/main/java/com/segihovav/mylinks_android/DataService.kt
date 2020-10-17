package com.segihovav.mylinks_android

import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.ArrayList

class DataService {
     companion object {
          @JvmStatic val getLinksDataEndpoint = "LinkData.php?task=fetchData"
          @JvmStatic val getTypesDataEndpoint = "LinkData.php?task=fetchTypes"
          @JvmStatic val deleteLinkDataEndpoint = "LinkData.php?task=deleteRow"
          @JvmStatic var MyLinksActiveURL: String = ""
          @JvmStatic lateinit var sharedPreferences: SharedPreferences
          @JvmStatic var myLinksTypes: ArrayList<MyLinkType> = ArrayList()
          @JvmStatic var myLinksTypeNames: ArrayList<String> = ArrayList()
          @JvmStatic var lightMode = R.style.ThemeOverlay_MaterialComponents
          @JvmStatic var darkMode = R.style.ThemeOverlay_MaterialComponents_Dark
          @JvmStatic var myLinkInstanceURLSNames: MutableList<String> = mutableListOf()
          @JvmStatic var MyLinksTitle: String = "AbaLinks"
          @JvmStatic var dataStore: ArrayList<FBDataStore> = ArrayList();

          @JvmStatic fun alert(builder: AlertDialog.Builder, message: String, closeApp: Boolean = false, confirmDialog: Boolean = false, finish: () -> Unit, OKCallback: (() -> Unit)?) {
               builder.setMessage(message).setCancelable(confirmDialog)

               builder.setPositiveButton("OK") { _, _ -> if (closeApp) finish() }

               if (confirmDialog) {
                    builder.setNegativeButton("Cancel") { _, _ -> if (closeApp) finish() }

                    if (OKCallback != null) {
                         builder.setPositiveButton("OK") { _, _ -> OKCallback() }
                    }
               }

               val alert = builder.create()

               alert.show()
          }

          @JvmStatic fun init() {
               // Read myLinks instance URLS from Firebase
               val database = FirebaseDatabase.getInstance()

               var myRef = database.getReference("MyLinks")

               myRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                         val dataList = dataSnapshot.children.toList()

                         dataStore.clear()
                         myLinkInstanceURLSNames.clear()

                         for (linkItem in dataList) {
                             dataStore.add(FBDataStore(linkItem.key.toString(),linkItem.value.toString()))
                             myLinkInstanceURLSNames.add(linkItem.key.toString())
                         }
                    }

                    override fun onCancelled(error: DatabaseError) {
                         // Failed to read value
                        //Log.w(TAG, "Failed to read value.", error.toException())
                    }
               })

               // Write the data to Firebase. Uncomment me if this data is deleted from the DB
               /*var myRefAdd = database.getReference("MyLinks/AbaLinks")
               myRefAdd.setValue("https://abalinks.hovav.org");

               myRefAdd = database.getReference("MyLinks/EmaLinks")
               myRefAdd.setValue("https://emalinks.hovav.org");*/
          }
     }
}
