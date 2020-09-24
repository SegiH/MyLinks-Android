package com.segihovav.abalinks_android

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

class AbaLinksAdapter(private val names: List<String>?) : RecyclerView.Adapter<MyViewHolder>() {
    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rowlayout, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Set displayed list item text
        holder.name.text= names!![position];

        holder.name.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount(): Int {
        return names?.size ?: 0
    }
}