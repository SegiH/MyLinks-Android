package com.segihovav.abalinks_android

import android.app.AlertDialog
import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.segihovav.abalinks_android.AbaLinksAdapter.MyViewHolder
import java.util.*

class AbaLinksAdapter(private val names: List<String>,private val abaLinksTypeNames: MutableList<String>) : RecyclerView.Adapter<MyViewHolder>() {
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val typeInfo: TextView = view.findViewById(R.id.linkTypeInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rowlayout, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Set displayed list item text
        if (names[position].contains("http") || names[position].contains("https")) {
            holder.name.text = Html.fromHtml(names[position], HtmlCompat.FROM_HTML_MODE_LEGACY)
            holder.name.movementMethod = LinkMovementMethod.getInstance()

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MainActivity.context))

            if (sharedPreferences.getBoolean("DarkThemeOn", false)) holder.name.setLinkTextColor(Color.rgb(255, 255, 255)) else holder.name.setLinkTextColor(Color.rgb(0, 0, 0))
        } else {
            holder.name.text = names[position]
        }

        if (position<abaLinksTypeNames.size)
             holder.typeInfo.text=abaLinksTypeNames[position]
    }

    /*private fun editLinkClick(v: View) {
        alert("Edit",false)
    }*/

    override fun getItemCount(): Int {
        return names.size
    }
}
