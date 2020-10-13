package com.segihovav.abalinks_android

//import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

// TO DO
class MainActivity : AppCompatActivity(), OnRefreshListener, AdapterView.OnItemSelectedListener {
     private lateinit var abaLinksURL: String
     private val abaLinksList: MutableList<AbaLink> = ArrayList()
     private var abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
     private val abaLinksTypeNames: java.util.ArrayList<String> = java.util.ArrayList()
     private lateinit var searchView: EditText
     private lateinit var sharedPreferences: SharedPreferences
     private lateinit var searchTypeIDSpinner: Spinner
     private lateinit var episodeListView: RecyclerView
     private val darkMode = R.style.Theme_MaterialComponents_DayNight
     private val lightMode = R.style.ThemeOverlay_MaterialComponents
     private var recyclerviewAdapter: RecyclerviewAdapter? = null
     private lateinit var touchListener: RecyclerTouchListener
     private val getLinksDataEndpoint = "LinkData.php?task=fetchData"
     private val getTypesDataEndpoint = "LinkData.php?task=fetchTypes"

     override fun onCreate(savedInstanceState: Bundle?) {
          sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

          this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

          super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_main)

          // Set context which is used by SwipeController
          context = applicationContext

          // Internet connection is always required
          if (!isNetworkAvailable(this))
               alert("No Internet connection detected. Internet access is needed to use this app.", closeApp = true)

          // Init the SwipeController
          val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

          // init swipe listener
          mSwipeRefreshLayout.setOnRefreshListener(this)
          mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark)
          mSwipeRefreshLayout.setOnRefreshListener { readJSONData("Links",getLinksDataEndpoint,::parseLinksJSON,true); searchView.text = searchView.text }

          episodeListView = findViewById(R.id.episodeList)

          abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != null) sharedPreferences.getString("AbaLinksURL", "").toString() else ""

          // Make sure that abaLinksURL always ends in a black slash
          if (abaLinksURL != "" && !abaLinksURL.endsWith("/"))
               abaLinksURL+="/"

          if (abaLinksURL == "")
               loadSettingsActivity()

          touchListener = RecyclerTouchListener(this, episodeListView)
          touchListener
          .setClickable(object : RecyclerTouchListener.OnRowClickListener {
               override fun onRowClicked(position: Int) {
                    //Toast.makeText(applicationContext, abaLinksList.get(position).Name, Toast.LENGTH_SHORT).show()
               }

               override fun onIndependentViewClicked(independentViewID: Int, position: Int) {}
          })
          .setSwipeOptionViews(R.id.open_link, R.id.delete_link, R.id.edit_link, R.id.share_link)
          .setSwipeable(R.id.rowFG, R.id.rowBG, object : RecyclerTouchListener.OnSwipeOptionsClickListener {
               override fun onSwipeOptionClicked(viewID: Int, position: Int) {
                    when (viewID) {
                         R.id.delete_link -> alert("Are you sure that you want to delete this link ?", closeApp = false, confirmDialog = true, deletingItemIndex = position)
                         R.id.edit_link -> {
                              val intent = Intent(context, EditActivity::class.java)

                              intent.putExtra(applicationContext.packageName + ".LinkItem", abaLinksList[position])
                              intent.putExtra(applicationContext.packageName + ".LinkTypes", abaLinksTypes)
                              intent.putExtra(applicationContext.packageName + ".LinkTypeNames", abaLinksTypeNames)

                              startActivity(intent)
                         }
                         R.id.open_link -> startActivity(Intent(Intent.ACTION_VIEW, parse(abaLinksList[position].URL)))
                         R.id.share_link -> {
                              val sendIntent: Intent = Intent().apply {
                                   action = Intent.ACTION_SEND
                                   putExtra(Intent.EXTRA_TEXT, abaLinksList[position].URL)
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
                         val abaLinksListFiltered: MutableList<AbaLink> = ArrayList()

                         for (i in abaLinksList.indices) {
                              // If the search term is contained in the name or URL
                              val itemName = abaLinksList[i].Name?.toLowerCase(Locale.ROOT)
                              val itemURL = abaLinksList[i].URL
                              val itemTypeID = abaLinksList[i].TypeID

                              val searchTerm = s.toString().toLowerCase(Locale.ROOT)
                              var searchTypeID = -1

                              for (j in abaLinksTypes.indices) {
                                   if (abaLinksTypes[j].Name == searchTypeIDSpinner.selectedItem)
                                        searchTypeID = abaLinksTypes[j].ID
                              }

                              when (searchTerm) {
                                   "" -> if ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)) {
                                              abaLinksListFiltered.add(abaLinksList[i])
                                         }
                                   else -> {
                                        if (itemName != null && itemName.contains(searchTerm) && ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)))
                                             abaLinksListFiltered.add(abaLinksList[i])
                                        else if (itemURL != null && itemURL.contains(s) && ((searchTypeID == -1 || searchTypeID == 6) || (searchTypeID != -1 && searchTypeID == itemTypeID)))
                                             abaLinksListFiltered.add(abaLinksList[i])
                                   }
                              }
                         }

                         // Call method that reloads the recycler view with the current data
                         initRecyclerView(abaLinksListFiltered)
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
                    var intent = Intent(this, EditActivity::class.java)

                    intent.putExtra(applicationContext.packageName + ".IsAdding", true)
                    intent.putExtra(applicationContext.packageName + ".LinkTypes", abaLinksTypes)
                    intent.putExtra(applicationContext.packageName + ".LinkTypeNames", abaLinksTypeNames)

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

          if (abaLinksURL != "")
               readJSONData("Types",getTypesDataEndpoint,::parseTypesJSON)

          if (episodeListView.adapter != null) episodeListView.adapter?.notifyDataSetChanged()
     }

     // Event when this activity returns from another activity
     public override fun onStart() {
          super.onStart()

          val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

          // Since onCreate() doesn't get called when returning from another activity, we have to set AbaLinksURL here
          abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != null) sharedPreferences.getString("AbaLinksURL", "").toString() else ""

          if (abaLinksURL != "" && !abaLinksURL.endsWith("/"))
               abaLinksURL+="/"

          if (abaLinksTypes.size == 0 && abaLinksURL != "")
               readJSONData("Types",getTypesDataEndpoint,::parseTypesJSON)
     }

     public override fun onStop() {
          super.onStop()
     }

     private fun alert(message: String, closeApp: Boolean, confirmDialog: Boolean = false, deletingItemIndex: Int = -1) {
          val builder = AlertDialog.Builder(this)

          builder.setMessage(message).setCancelable(confirmDialog)

          // When deleting, we need  yes/no cancelable buttons
          if (confirmDialog && deletingItemIndex != -1) {
               builder.setNegativeButton("Cancel") { _, _ -> if (closeApp) finish() }

               builder.setPositiveButton("OK") { _, _ ->
                    val deleteLinkDataEndpoint = "LinkData.php?task=deleteRow"

                    val params= "&LinkID=${abaLinksList[deletingItemIndex].ID}"

                    //processData(getLinkDataEndpoint, params)

                    val requestQueue: RequestQueue = Volley.newRequestQueue(this)

                    val request = JsonArrayRequest(
                            Request.Method.GET, abaLinksURL + deleteLinkDataEndpoint + params, null,
                            { _ ->
                            },
                            {
                                 //System.out.println("****** Error response=" + error.toString());
                                 //alert("An error occurred deleting the link with the error ", closeApp = false)
                            })

                    requestQueue.add(request)

                    abaLinksList.removeAt(deletingItemIndex)

                    recyclerviewAdapter?.notifyDataSetChanged()

                    alert("Item has been deleted", closeApp = false)
               }
          } else { // informational alert only
               builder.setPositiveButton("OK") { _, _ -> if (closeApp) finish() }
          }

          val alert = builder.create()

          alert.show()
     }

     private fun initRecyclerView(arrayList: List<AbaLink>) {
          val abaLinkNames: MutableList<String>
          val abaLinkTypeNames: MutableList<String> = ArrayList() // Used to save the typw
          val layoutManager: RecyclerView.LayoutManager
          val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

          // Creates names array used as the item label for the RecyclerView
          abaLinkNames = ArrayList()
          abaLinkNames.clear()

          for (i in arrayList.indices) {
               abaLinkNames.add("<A HREF=${arrayList[i].URL}>${arrayList[i].Name}</A>")

               // Type names
               for (j in abaLinksTypes.indices) {
                    if (abaLinksTypes[j].ID==arrayList[i].TypeID && abaLinksTypes[j].Name != "")
                         abaLinkTypeNames.add(abaLinksTypes[j].Name.toString())
               }
          }

          recyclerviewAdapter = RecyclerviewAdapter(this, arrayList as MutableList<AbaLink>, abaLinksTypes)

          recyclerviewAdapter?.setDarkMode(if (sharedPreferences.getBoolean("DarkThemeOn", false)) true else false)

          layoutManager = LinearLayoutManager(applicationContext)

          episodeListView.layoutManager = layoutManager
          episodeListView.itemAnimator = DefaultItemAnimator()

          registerForContextMenu(episodeListView)

          mSwipeRefreshLayout.isRefreshing = false

          episodeListView.setAdapter(recyclerviewAdapter)
     }

     private fun isNetworkAvailable(context: Context): Boolean {
          return try {
               val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
          } catch (e: Exception) {
               false
          }
     }

     private fun loadSettingsActivity() {
          val intent = Intent(this, SettingsActivity::class.java)

          // When resuming this activity, hide the search field and search type dropdown
          if (searchView.visibility == View.VISIBLE)
               searchViewIsVisible()

          startActivity(intent)
     }

     private fun parseLinksJSON(JSONData: JSONArray,isRefreshing: Boolean = false) {
          // This is needed so that when the user pulls to refresh, all previous items are removed to avoid duplicates
          abaLinksList.clear()

          for (i in 0 until JSONData.length()) {
               try {
                    val jsonobject = JSONData.getJSONObject(i)

                    abaLinksList.add(AbaLink(jsonobject.getString("ID").toInt(), jsonobject.getString("Name"), jsonobject.getString("URL"), jsonobject.getString("TypeID").toInt()))
               } catch (e: JSONException) {
                    e.printStackTrace()
                    alert("An error occurred reading the links. Please check your network connection or the URL in Settings", closeApp = false)
               }
          }

          if (!isRefreshing)
               initRecyclerView(abaLinksList)
     }

     private fun parseTypesJSON(JSONData: JSONArray,isRefreshing: Boolean = false) {
          abaLinksTypes.clear()
          abaLinksTypeNames.clear()

          for (i in 0 until JSONData.length()) {
               try {
                    val jsonobject = JSONData.getJSONObject(i)

                    abaLinksTypes.add(AbaLinkType(jsonobject.getInt("id"), jsonobject.getString("value")))

                    abaLinksTypeNames.add(jsonobject.getString("value"))
               } catch (e: JSONException) {
                    e.printStackTrace()
               }
          }

          // Creating adapter for spinner
          val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, abaLinksTypeNames)

          // attaching data adapter to spinner
          searchTypeIDSpinner.adapter = dataAdapter

          readJSONData("Links",getLinksDataEndpoint,::parseLinksJSON,isRefreshing)
     }

     // Read JSON from specific endpoint and call the specified callback after it has been fetched
     private fun readJSONData(dataType: String, endpoint: String, JSONCallback: (JSONData: JSONArray, isRefreshing: Boolean) -> Unit, isRefreshing: Boolean = false) {
          val requestQueue: RequestQueue = Volley.newRequestQueue(this)

          val request = JsonArrayRequest(Request.Method.GET, abaLinksURL + endpoint, null,
                  { response ->
                       var jsonarray: JSONArray = JSONArray()

                       try {
                            jsonarray = JSONArray(response.toString())
                       } catch (e: JSONException) {
                            e.printStackTrace()
                       }

                       JSONCallback(jsonarray,isRefreshing)
                  },
                  {
                       //System.out.println("****** Error response=" + error.toString());
                       alert("An error occurred reading the $dataType with the error $it. Please check your network connection", closeApp = false)
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

     // Supresses warning about the class property
     companion object {
          @JvmField
          //@SuppressLint("StaticFieldLeak")
          var context: Context? = null
     }
}