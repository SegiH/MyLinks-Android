package com.segihovav.mylinks_android

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.segihovav.mylinks_android.DataService.Companion.MyLinksActiveURL
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

// TO DO
class MainActivity : AppCompatActivity(), OnRefreshListener, AdapterView.OnItemSelectedListener {
     private val myLinksList: MutableList<MyLink> = ArrayList()
     private lateinit var searchView: EditText
     private lateinit var searchTypeIDSpinner: Spinner
     private lateinit var episodeListView: RecyclerView
     private var isLoading: Boolean = false
     private var recyclerviewAdapter: RecyclerviewAdapter? = null
     private lateinit var touchListener: RecyclerTouchListener

     override fun onCreate(savedInstanceState: Bundle?) {
          DataService.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

          DataService.init()

          this.setTheme(if (DataService.sharedPreferences.getBoolean("DarkThemeOn", false)) DataService.darkMode else DataService.lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.mainactivity)

          // Internet connection is always required
          if (!isNetworkAvailable())
               DataService.alert(builder=AlertDialog.Builder(this), message="No Internet connection detected. Internet access is needed to use this app.", closeApp=true, finish={ finish() }, OKCallback=null)

          MyLinksActiveURL = if (DataService.sharedPreferences.getString("MyLinksActiveURL", "") != null) DataService.sharedPreferences.getString("MyLinksActiveURL", "").toString() else ""

          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          this.isLoading = true

          val request = JsonArrayRequest(Request.Method.GET, DataService.JSONBaseURL +  "?MyLinks-Instances-Auth=" + DataService.JSONAuthToken + "&task=getURLs", null,
               { response ->
                    var jsonarray: JSONArray = JSONArray()

                    try {
                         this.isLoading = false

                         jsonarray = JSONArray(response.toString())

                         DataService.instanceURLs.clear()

                         for (i in 0 until jsonarray.length()) {
                              try {
                                   val jsonobject = jsonarray.getJSONObject(i)

                                   if (MyLinksActiveURL == jsonobject.getString("URL"))
                                        DataService.MyLinksTitle =  jsonobject.getString("DisplayName");

                                   title=jsonobject.getString("DisplayName")

                                   DataService.instanceURLs.add(InstanceURLType(jsonobject.getString("Name"), jsonobject.getString("URL"), jsonobject.getString("DisplayName")))
                              } catch (e: JSONException) {
                                   //e.printStackTrace()
                                   DataService.alert(builder = AlertDialog.Builder(this), message = "An error occurred reading the links. Please check your network connection or the URL in Settings", finish = { finish() }, OKCallback = null)
                              }
                         }

                         readJSONData("Links",DataService.getLinksDataEndpoint,::parseLinksJSON,true);
                    } catch (e: JSONException) {
                         this.isLoading = true
                         e.printStackTrace()
                    }
               },
               {
                    DataService.alert(builder= AlertDialog.Builder(this), message="An error occurred reading the instances with the error $it. Please check your network connection", finish={ finish() }, OKCallback=null)
               }
          )

          requestQueue.add(request)

          // Init the SwipeController
          val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

          // init swipe listener
          mSwipeRefreshLayout.setOnRefreshListener(this)
          mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark)
          mSwipeRefreshLayout.setOnRefreshListener { if (this != null && !this.isLoading) readJSONData("Links",DataService.getLinksDataEndpoint,::parseLinksJSON,true); searchView.text = searchView.text }

          episodeListView = findViewById(R.id.episodeList)

          // Make sure that MyLinksURL always ends in a black slash
          if (MyLinksActiveURL != "" && !MyLinksActiveURL.endsWith("/"))
               MyLinksActiveURL+="/"

          if (MyLinksActiveURL == "")
               loadSettingsActivity()

          val builder=AlertDialog.Builder(this)

          touchListener = RecyclerTouchListener(this, episodeListView)
          touchListener
          .setClickable(object : RecyclerTouchListener.OnRowClickListener {
               override fun onRowClicked(position: Int) { /* Toast.makeText(applicationContext, myLinksList.get(position).Name, Toast.LENGTH_SHORT).show() */ }

               override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
          })
          .setSwipeOptionViews(R.id.open_link, R.id.delete_link, R.id.edit_link, R.id.share_link)
          .setSwipeable(R.id.rowFG, R.id.rowBG, object : RecyclerTouchListener.OnSwipeOptionsClickListener {
               override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                    when (viewID) {
                         R.id.delete_link -> DataService.alert(builder, message="Are you sure that you want to delete this link ?", confirmDialog=true, finish={ finish() }) { deleteRow((position)) }
                         R.id.edit_link -> {
                              val intent = Intent(applicationContext, AddEditLinkActivity::class.java)

                              intent.putExtra(applicationContext.packageName + ".LinkItem", myLinksList[position])

                              startActivity(intent)
                         }
                         R.id.open_link -> startActivity(Intent(Intent.ACTION_VIEW, parse(myLinksList[position].URL)))
                         R.id.share_link -> {
                              val sendIntent: Intent = Intent().apply {
                                   action = Intent.ACTION_SEND
                                   putExtra(Intent.EXTRA_TEXT, myLinksList[position].URL)
                                   type = "text/plain"
                              }

                              val shareIntent = Intent.createChooser(sendIntent, null)
                              startActivity(shareIntent)
                         }
                    }
               }
          })

          episodeListView.addOnItemTouchListener(touchListener)

          searchView = findViewById(R.id.searchView)

          searchTypeIDSpinner = findViewById<Spinner>(R.id.searchTypeIDSpinner)

          searchTypeIDSpinner.onItemSelectedListener = this

          // Search change event
          searchView.addTextChangedListener(object : TextWatcher {
               override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
               override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s != "") {
                         val myLinksListFiltered: MutableList<MyLink> = ArrayList()

                         for (i in myLinksList.indices) {
                              // If the search term is contained in the name or URL
                              val itemName = myLinksList[i].Name?.toLowerCase(Locale.ROOT)
                              val itemURL = myLinksList[i].URL
                              val itemTypeID = myLinksList[i].TypeID

                              val searchTerm = s.toString().toLowerCase(Locale.ROOT)
                              var searchTypeID = -1

                              for (j in DataService.myLinksTypes.indices) {
                                   if (DataService.myLinksTypes[j].Name == searchTypeIDSpinner.selectedItem)
                                        searchTypeID = DataService.myLinksTypes[j].ID
                              }

                              when (searchTerm) {
                                   "" -> if ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)) {
                                              myLinksListFiltered.add(myLinksList[i])
                                         }
                                   else -> {
                                        if (itemName != null && itemName.contains(searchTerm) && ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)))
                                             myLinksListFiltered.add(myLinksList[i])
                                        else if (itemURL != null && itemURL.contains(s) && ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)))
                                             myLinksListFiltered.add(myLinksList[i])
                                   }
                              }
                         }

                         // Call method that reloads the recycler view with the current data
                         initRecyclerView(myLinksListFiltered)
                    }
               }

               override fun afterTextChanged(s: Editable) {}
          })
     }

     override fun onCreateOptionsMenu(menu: Menu): Boolean {
          // Inflate the menu; this adds items to the action bar if it is present.
          menuInflater.inflate(R.menu.menu_main, menu)
          val searchMenuItem = menu.findItem(R.id.action_search)

          // Search search menu icon based on the current theme
          searchMenuItem.setIcon(R.drawable.search_white)

          return true
     }

     override fun onConfigurationChanged(newConfig: Configuration) {
          super.onConfigurationChanged(newConfig)

           if (episodeListView.adapter != null)
                episodeListView.adapter?.notifyDataSetChanged()
     }

     override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
          // Set text to itself if the user selects a type filter to trigger the searchView.addTextChangedListener event
          searchView.text = searchView.text
     }

     override fun onNothingSelected(p0: AdapterView<*>) { }

     override fun onOptionsItemSelected(item: MenuItem): Boolean {
          when (item.itemId) {
               R.id.action_settings -> { // Settings menu
                    loadSettingsActivity()
                    return true
               }
               R.id.action_add -> { // Add menu
                    var intent = Intent(this, AddEditLinkActivity::class.java)

                    intent.putExtra(applicationContext.packageName + ".IsAdding", true)

                    startActivity(intent)
               }
               R.id.action_search -> // Search menu
                    searchViewIsVisible()
          }

          return super.onOptionsItemSelected(item)
     }

     override fun onRefresh() { }

     // Fixes the issue that causes the swipe buttons to disappear when leaving the app
     public override fun onResume() {
          super.onResume()

          title=DataService.getActiveInstanceDisplayName()

          if (!this.isLoading)
               readJSONData("Types", if (DataService.getActiveInstanceName() == "SegiLinks")  DataService.getSegiTypesDataEndpoint else DataService.getTypesDataEndpoint,::parseTypesJSON)

          recyclerviewAdapter?.notifyDataSetChanged()

          if (episodeListView.adapter != null) episodeListView.adapter?.notifyDataSetChanged()
     }

     // Event when this activity returns from another activity
     public override fun onStart() {
          super.onStart()

          if (MyLinksActiveURL != "" && !MyLinksActiveURL.endsWith("/"))
               MyLinksActiveURL +="/"

          if (DataService.myLinksTypes.size == 0 && DataService.getActiveInstanceDisplayName() != "" && !this.isLoading)
               readJSONData("Types",if (DataService.getActiveInstanceName() == "SegiLinks")  DataService.getSegiTypesDataEndpoint else DataService.getTypesDataEndpoint,::parseTypesJSON)
     }

     public override fun onStop() {
          super.onStop()
     }

     private fun deleteRow(deletingItemIndex: Int = -1) {
          if (deletingItemIndex == -1) {
               DataService.alert(builder=AlertDialog.Builder(this), message="deletingItemIndex was not provided", finish={ finish() }, OKCallback=null)
               return
          }

          val params= "&LinkID=${myLinksList[deletingItemIndex].ID}"

          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(
                  Request.Method.GET, MyLinksActiveURL + DataService.deleteLinkDataEndpoint + params, null,
                  { _ ->
                  },
                  {
                       //System.out.println("****** Error response=" + error.toString());
                       DataService.alert(builder=AlertDialog.Builder(this), message="An error occurred deleting the link with the error " + it.toString(), finish={ finish() }, OKCallback=null)
                  })

          requestQueue.add(request)

          myLinksList.removeAt(deletingItemIndex)

          recyclerviewAdapter?.notifyDataSetChanged()

          DataService.alert(builder=AlertDialog.Builder(this), message="Item has been deleted", finish={ finish() }, OKCallback=null)
     }

     private fun initRecyclerView(arrayList: List<MyLink>) {
          val myLinkNames: MutableList<String>
          val layoutManager: RecyclerView.LayoutManager
          val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

          // Creates names array used as the item label for the RecyclerView
          myLinkNames = ArrayList()
          myLinkNames.clear()

          for (i in arrayList.indices)
               myLinkNames.add("<A HREF=${arrayList[i].URL}>${arrayList[i].Name}</A>")

          recyclerviewAdapter = RecyclerviewAdapter(this, arrayList as MutableList<MyLink>, DataService.myLinksTypes)

          recyclerviewAdapter?.setDarkMode(DataService.sharedPreferences.getBoolean("DarkThemeOn", false))

          layoutManager = LinearLayoutManager(applicationContext)

          episodeListView.layoutManager = layoutManager
          episodeListView.itemAnimator = DefaultItemAnimator()

          registerForContextMenu(episodeListView)

          mSwipeRefreshLayout.isRefreshing = false

          episodeListView.adapter = recyclerviewAdapter
     }

     private fun isNetworkAvailable(): Boolean {
          return try {
               val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
          } catch (e: Exception) {
               false
          }
     }

     private fun loadSettingsActivity() {
          val intent = Intent(this, SettingsActivity::class.java)

          // When resuming this activity, hide the search field and search type dropdown
          // if (searchView != null && searchView.visibility == View.VISIBLE)
          //      searchViewIsVisible()

          startActivity(intent)
     }

     private fun parseLinksJSON(JSONData: JSONArray,isRefreshing: Boolean = false) {
          // This is needed so that when the user pulls to refresh, all previous items are removed to avoid duplicates
          myLinksList.clear()

          for (i in 0 until JSONData.length()) {
               try {
                    val jsonobject = JSONData.getJSONObject(i)

                    myLinksList.add(MyLink(jsonobject.getString("ID").toInt(), jsonobject.getString("Name"), jsonobject.getString("URL"), jsonobject.getString("TypeID").toInt()))
               } catch (e: JSONException) {
                    //e.printStackTrace()
                    DataService.alert(builder=AlertDialog.Builder(this),message ="The error $e occurred in parseLinksJSON() when JSONData is $JSONData and is refreshing status is $isRefreshing Please check your network connection or the URL in Settings", finish={ finish() }, OKCallback=null) }
          }

          initRecyclerView(myLinksList)
     }

     private fun parseTypesJSON(JSONData: JSONArray,isRefreshing: Boolean = false) {
          DataService.myLinksTypes.clear()
          DataService.myLinksTypeNames.clear()

          for (i in 0 until JSONData.length()) {
               try {
                    val jsonobject = JSONData.getJSONObject(i)

                    DataService.myLinksTypes.add(MyLinkType(jsonobject.getInt("id"), jsonobject.getString("value")))

                    DataService.myLinksTypeNames.add(jsonobject.getString("value"))
               } catch (e: JSONException) {
                    e.printStackTrace()
               }
          }

          // Creating adapter for spinner
          val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, DataService.myLinksTypeNames)

          // attaching data adapter to spinner
          searchTypeIDSpinner.adapter = dataAdapter

          readJSONData("Links",DataService.getLinksDataEndpoint + "&InstanceName=" + DataService.getActiveInstanceName(),::parseLinksJSON,isRefreshing)
     }

     // Read JSON from specific endpoint and call the specified callback after it has been fetched
     private fun readJSONData(dataType: String, endpoint: String, JSONCallback: ((JSONData: JSONArray, isRefreshing: Boolean) -> Unit)?, isRefreshing: Boolean = false) {
          if (MyLinksActiveURL == "")
               return

          this.isLoading = true;

          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val instanceName= DataService.getActiveInstanceName()
          val newEndpoint= "$endpoint&InstanceName=$instanceName"

          val request = JsonArrayRequest(Request.Method.GET, MyLinksActiveURL + newEndpoint, null,
                  { response ->
                       this.isLoading = false;

                       var jsonarray: JSONArray = JSONArray()

                       try {
                            jsonarray = JSONArray(response.toString())
                       } catch (e: JSONException) {
                            e.printStackTrace()
                       }

                       if (JSONCallback != null) {
                            JSONCallback(jsonarray,isRefreshing)
                       }
                  },
                  {
                       this.isLoading = false
                       DataService.alert(builder=AlertDialog.Builder(this), message="An error $it occurred in readJSONData when DataService.MyLinksActiveURL=$DataService.$MyLinksActiveURL An error occurred reading the $dataType with the error $it while retrieving from $DataService.MyLinksActiveURL $endpoint. Please check your network connection", finish={ finish() }, OKCallback=null)
                  }
          )
          requestQueue.add(request)

     }

     // Visibility is always toggled
     private fun searchViewIsVisible() {
          val swipeControl = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

          val isHidden= searchView.visibility != View.VISIBLE

          searchView.visibility = if (!isHidden) View.GONE else View.VISIBLE
          searchView.requestFocus()

          searchTypeIDSpinner.visibility = if (!isHidden) View.GONE else View.VISIBLE

          // Set top margin on the swipeControl which contains the episode Recyclerview when showing the search field and search type dropdown
          val params: ViewGroup.MarginLayoutParams = swipeControl.layoutParams as ViewGroup.MarginLayoutParams

          if (isHidden) {
               params.setMargins(0, 300, 0, 0)
          } else {
               params.setMargins(0, 0, 0, 0)
          }

          swipeControl.layoutParams=params
     }
}
