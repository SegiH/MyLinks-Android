package com.segihovav.abalinks_android

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.android.volley.BuildConfig
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

// TO DO/Fix
// DONE - in edit panel move all fields more to the left
// DONE - reload data after returning from editactivity
// DONE - add URL on main list
// DONE - add additional info below name
// DONE - remove the text editactivity at the top of the editactivity
// DONR - Spinner not selecting the right value
// DONE - Implement adding
// DONE - add error chcking and close app if a fatal error occurs
// DONE - implement deleting

// when saving, creates duplicate
// Replace !! with ? in all classes
// add search
// change app icon

class MainActivity : AppCompatActivity(), OnRefreshListener {
    private lateinit var abaLinksURL: String
    private val abaLinksList: MutableList<AbaLink> = ArrayList()
    private var abaLinksTypes: ArrayList<AbaLinkType> = ArrayList()
    private lateinit var searchView: EditText
    private lateinit var swipeController: SwipeController
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var typeIDSpinner: Spinner
    private lateinit var episodeListView: RecyclerView

    private val darkMode = R.style.Theme_AppCompat_DayNight
    private val lightMode = R.style.ThemeOverlay_MaterialComponents

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        this.setTheme(if (sharedPreferences.getBoolean("DarkThemeOn", false)) darkMode else lightMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set context which is used by SwipeController
        context = applicationContext

        // Internet connection is always required
        if (!isNetworkAvailable(this))
            alert("No Internet connection detected. Internet access is needed to use this app.", true)

        // Init the SwipeController
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)
        swipeController = SwipeController(object : SwipeControllerActions() {}, abaLinksList)
        swipeController.setMainActivity(this);

        // init swipe listener
        mSwipeRefreshLayout.setOnRefreshListener(this)
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_blue_dark)
        mSwipeRefreshLayout.setOnRefreshListener { loadJSONData() }

        episodeListView = findViewById(R.id.episodeList)

        abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != "") sharedPreferences.getString("AbaLinksURL", "") + (if (!sharedPreferences.getString("AbaLinksURL", "")!!.endsWith("/")) "/" else "") else ""

        if (abaLinksURL == "") {
            alert("Please set the URL to your instance of AbaLinks in Settings", false)
            return
        }

        searchView = findViewById(R.id.searchView)

        typeIDSpinner = findViewById<Spinner>(R.id.TypeIDSpinner)

        // Hide by default
        searchViewIsVisible(false)

        // Set the searchView icon based on the theme
        if (sharedPreferences.getBoolean("DarkThemeOn", false)) searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_white, 0, 0, 0) else searchView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.search_black, 0, 0, 0)

        // Search change event
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s != "") {
                    val abaLinksListFiltered: MutableList<AbaLink> = ArrayList()

                    for (i in abaLinksList.indices) {
                        // If the search term is contained in the name or URL
                        if (abaLinksList != null && abaLinksList[i] != null && abaLinksList[i].Name!= null && abaLinksList[i].Name?.toLowerCase(Locale.ROOT) != null && abaLinksList[i].Name?.toLowerCase(Locale.ROOT)?.contains(s.toString().toLowerCase(Locale.ROOT))!! || abaLinksList[i].URL.toString().contains(s)) {
                            abaLinksListFiltered.add(abaLinksList[i])
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
        searchMenuItem.setIcon(if (sharedPreferences.getBoolean("DarkThemeOn", false)) R.drawable.search_white else R.drawable.search_black)
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
       super.onConfigurationChanged(newConfig)

       if (episodeListView.adapter != null) episodeListView.adapter?.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // Make search view hidden by default. It will be shown if needed
        //searchViewIsVisible(false)

        if (id == R.id.action_settings) { // Settings menu
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.action_add) {
            val intent = Intent(this, EditActivity::class.java)

            intent.putExtra("com.segihovav.abalinks_android.IsAdding", true)

            var counter=0

            // This seems to be the only way to copy the link types to another intent since I can't figure out how to use linkTypes as a value for putextra
            for (i in abaLinksTypes.indices) {
                intent.putExtra("com.segihovav.abalinks_android.LinkTypeID" + counter, abaLinksTypes[counter].ID.toString())
                intent.putExtra("com.segihovav.abalinks_android.LinkTypeName" + counter, abaLinksTypes[counter].Name)
                counter++
            }

            startActivity(intent)
        } else if (id == R.id.action_search) {
            searchViewIsVisible(true)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() { }

    // Fixes the issue that causes the swipe buttons to disappear when leaving the app
    public override fun onResume() {
        super.onResume()

        loadTypes()

        if (episodeListView.adapter != null) episodeListView.adapter?.notifyDataSetChanged()
    }

    // Event when this activity returns from another activity
    public override fun onStart() {
        super.onStart()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Since onCreate() doesn't get called when returning from another activity, we have to set AbaLinksURL here
        abaLinksURL = if (sharedPreferences.getString("AbaLinksURL", "") != "") sharedPreferences.getString("AbaLinksURL", "") + (if (sharedPreferences != null && !sharedPreferences.getString("AbaLinksURL", "")!!.endsWith("/")) "/" else "") else ""

        if (abaLinksTypes.size == 0)
             loadTypes()
    }

    public override fun onStop() {
        super.onStop()

        // Hide by default
        searchViewIsVisible(false)
    }

    private fun alert(message: String, closeApp: Boolean) {
        // Display dialog
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ -> if (closeApp) finish() }
        val alert = builder.create()

        alert.show()
    }

    private fun initRecyclerView(arrayList: List<AbaLink>) {
        val abaLinkNames: MutableList<String>
        val abaLinkTypeNames: MutableList<String> = ArrayList() // Used to save the typw
        val adapter: AbaLinksAdapter
        val layoutManager: RecyclerView.LayoutManager
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

        // Creates names array used as the item label for the RecyclerView
        abaLinkNames = ArrayList()
        abaLinkNames.clear()

        for (i in arrayList.indices) {
            //arrayList[i].Name?.let { abaLinkNames.add(it) };
            abaLinkNames.add("<A HREF='" + arrayList[i].URL + "'>" + arrayList[i].Name + "</A>")

            // Type names
            for (j in abaLinksTypes.indices) {
                if (abaLinksTypes[j].ID==arrayList[i].TypeID && abaLinksTypes[j] != null && abaLinksTypes[j].Name != "") {
                    abaLinkTypeNames.add(abaLinksTypes[j].Name!!)
                }
            }
        }

        // specify an adapter (see also next example)
        adapter = AbaLinksAdapter(abaLinkNames, abaLinkTypeNames)
        adapter.notifyDataSetChanged()

        layoutManager = LinearLayoutManager(applicationContext)


        episodeListView.layoutManager = layoutManager
        episodeListView.itemAnimator = DefaultItemAnimator()

        swipeController.setAbaLinksList(arrayList)

        val itemTouchhelper = ItemTouchHelper(swipeController)
        itemTouchhelper.attachToRecyclerView(episodeListView)

        episodeListView.adapter = adapter
        registerForContextMenu(episodeListView)

        mSwipeRefreshLayout.isRefreshing = false
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) != null
        } catch (e: Exception) {
            true
        }
    }

    private fun loadJSONData() {
        val getLinkDataEndpoint = "LinkData.php?task=fetchData"
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val request = JsonArrayRequest(
                Request.Method.GET,
                abaLinksURL + getLinkDataEndpoint,
                null,
                Response.Listener { response ->
                    lateinit var jsonarray: JSONArray
                    try {
                        jsonarray = JSONArray(response.toString())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    // This is needed so that when the user pulls to refresh, all previous items are removed  to avoid duplicates
                    abaLinksList.clear()

                    if (BuildConfig.DEBUG && jsonarray == null) {
                        error("Assertion failed")
                    }

                    for (i in 0 until jsonarray.length()) {
                        try {
                            val jsonobject = jsonarray.getJSONObject(i)

                            abaLinksList.add(AbaLink(jsonobject.getString("ID").toInt(), jsonobject.getString("Name"), jsonobject.getString("URL"), jsonobject.getString("TypeID").toInt()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            alert("An error occurred reading the links. Please check your network connection", true)
                        }
                    }

                    initRecyclerView(abaLinksList)
                },
                Response.ErrorListener {
                    //System.out.println("****** Error response=" + error.toString());
                    alert("An error occurred reading the links. Please check your network connection", true)
                })
        requestQueue.add(request)
    }

    private fun loadTypes() {
         val getLinkDataEndpoint = "LinkData.php?task=fetchTypes"
         val requestQueue: RequestQueue = Volley.newRequestQueue(this)
         val request = JsonArrayRequest(
                 Request.Method.GET,
                 abaLinksURL + getLinkDataEndpoint,
                 null,
                 Response.Listener { response ->
                     lateinit var jsonarray: JSONArray

                     try {
                         jsonarray = JSONArray(response.toString())
                     } catch (e: JSONException) {
                         e.printStackTrace()
                     }

                     if (BuildConfig.DEBUG && jsonarray == null) {
                         error("Assertion failed")
                     }

                     abaLinksTypes.clear()

                     for (i in 0 until jsonarray.length()) {
                         try {
                             val jsonobject = jsonarray.getJSONObject(i)

                             abaLinksTypes.add(AbaLinkType(jsonobject.getInt("id"), jsonobject.getString("value")))
                         } catch (e: JSONException) {
                             e.printStackTrace()
                         }
                     }

                     swipeController.setLinkTypes(abaLinksTypes)

                     loadJSONData()
                 },
                 Response.ErrorListener {
                     alert("An error occurred reading the types. Please check your network connection", true)
                 })
        requestQueue.add(request)
    }

    // When showing the search EditText, move the entire swipe layout down and then move it back up when hiding the search
    private fun searchViewIsVisible(isHidden: Boolean) {
        val swipeControl = findViewById<SwipeRefreshLayout>(R.id.swipe_container)

        val marginLayoutParams = swipeControl.layoutParams as MarginLayoutParams
        marginLayoutParams.setMargins(marginLayoutParams.marginStart, if (isHidden) 130 else 0, marginLayoutParams.marginEnd, marginLayoutParams.bottomMargin)

        swipeControl.layoutParams = marginLayoutParams

        val spinnerMargin = typeIDSpinner.layoutParams as MarginLayoutParams

        //val episodeListmarginLayoutParams = episodeListView?.layoutParams as MarginLayoutParams

        //val layoutParams: ViewGroup.LayoutParams = episodeListView.getLayoutParams()

        //var params: MarginLayoutParams=episodeListView.layoutParams

        //episodeListView.getLayoutParams().width = 200 params . leftMargin = 100
        //params.topMargin = 200

        if (isHidden) {
            //spinnerMargin.setMargins(0,200,0,150)
            //episodeListmarginLayoutParams.setMargins(0,50,0,0)
            //episodeListmarginLayoutParams.se .setMargins(0,50,0,0)
            //episodeListView.layoutParams(params)
            //episodeListView.setPadding(0, 100, 0, 0)
        } else {
            //spinnerMargin.setMargins(0,100,0,0)
            //episodeListmarginLayoutParams.setMargins(0,50,0,0)
            //episodeListmarginLayoutParams.setMargins(0,0,0,0)
            //episodeListView.setPadding(0, 0, 0, 0)
        }

        //typeIDSpinner.layoutParams=spinnerMargin
        //episodeListView.layoutParams=episodeListmarginLayoutParams

        searchView = findViewById(R.id.searchView)
        searchView.visibility = if (!isHidden) View.GONE else View.VISIBLE
        searchView.requestFocus()

        typeIDSpinner = findViewById<Spinner>(R.id.TypeIDSpinner)
        typeIDSpinner.visibility = if (!isHidden) View.GONE else View.VISIBLE

        episodeListView.refreshDrawableState()
    }

    // Supresses warning about the class property
    companion object {
        @JvmField
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }
}
