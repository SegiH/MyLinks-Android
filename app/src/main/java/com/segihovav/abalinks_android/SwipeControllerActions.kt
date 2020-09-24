package com.segihovav.abalinks_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import java.util.*

abstract class SwipeControllerActions {
    private lateinit var requestQueue: RequestQueue

    // Edit
    fun onLeftClicked(episode: List<AbaLinks>, position: Int,mainActivity: AppCompatActivity) {
        //mainActivity.intent = = Intent(this, EditActivity::class.java)
        val intent = Intent(mainActivity, EditActivity::class.java)
        mainActivity.startActivity(intent)
        //startActivity(Edi)

        //val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MainActivity.context))
        //val abaLinksURL = sharedPreferences.getString("AbaLinksURL", "") + if (!sharedPreferences.getString("AbaLinksURL", "")!!.endsWith("/")) "/" else ""
        //val updateCheckInOut = Link"WTF.php?CheckInOut"

        // Toggle check in out value
        //episode[position].toggleCheckedInOut()

        // Call REST endpoint to update the favorite for the current episode
        /*requestQueue = Volley.newRequestQueue(MainActivity.context)
        val request = JsonObjectRequest(
                Request.Method.GET,
                abaLinksURL + updateCheckInOut + "&EpisodeNumber=" + episode[position].episodeNumber + "&IsCheckedOut=" + !episode[position].isCheckedIn,
                null,
                // This line causes an error because the server side returns false on success and the error is com.android.volley.ParseError: org.json.JSONException: Value false of type java.lang.Boolean cannot be converted to JSONObject. This can only be fixed server side so ignore this false error for now (no pun intended)
                Response.Listener { },
                Response.ErrorListener { error ->
                    // The REST endpoint returns an error
                    println("****** Error response when updating check in/out=$error") // + " " + WTFIndexerURL + updateCheckInOut + "&EpisodeNumber=" + episodes.get(position).getEpisodeNumber() + "&IsCheckedOut=" + !episodes.get(position).getIsCheckedIn());
                })
        requestQueue.add(request)*/
    }

    // Favorite button event
    fun onRightClicked(episode: List<AbaLinks>, position: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MainActivity.context))
        val wtfIndexerURL = sharedPreferences.getString("WTFIndexerURL", "") + if (!sharedPreferences.getString("WTFIndexerURL", "")!!.endsWith("/")) "/" else ""
        val updateEpisodeFavoriteEndpoint = "WTF.php?UpdateFavorite"


        // Toggle favorite value
       // episode[position].toggleFavorite()

        // Call REST endpoint to update the favorite for the current episode
        requestQueue = Volley.newRequestQueue(MainActivity.context)
        /*val request = JsonArrayRequest(
                Request.Method.GET,
                wtfIndexerURL + updateEpisodeFavoriteEndpoint + "&EpisodeNumber=" + episode[position].episodeNumber + "&FavoriteValue=" + episode[position].favorite,
                null,
                Response.Listener { },
                Response.ErrorListener {
                    // The REST endpoint returns an error because updateEpisodeFavoriteEndpoint doesn't return anything but the favorite updates anyways so ignore this error for now
                    //System.out.println("****** Error response when updating favorite=" + error.toString() + " " + WTFIndexerURL + updateEpisodeFavoriteEndpoint + "&EpisodeNumber=" + episodes.get(position).getEpisodeNumber() + "&FavoriteValue=" + episodes.get(position).getFavorite());
                })
        requestQueue.add(request)*/
    }
}