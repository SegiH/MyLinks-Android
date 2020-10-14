package com.segihovav.abalinks_android

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerviewAdapter internal constructor(private val mContext: Context,private val abaLinks: MutableList<AbaLink>,private val abaLinksTypes: MutableList<AbaLinkType>) : RecyclerView.Adapter<RecyclerviewAdapter.MyViewHolder>() {
     var darkMode: Boolean = false
     var rowFG: LinearLayout? = null
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
          val view: View = LayoutInflater.from(mContext).inflate(R.layout.link_item, parent, false)
          rowFG=view.findViewById(R.id.rowFG)
          return MyViewHolder(view)
     }

     override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
          val abaLinkItem: AbaLink = abaLinks[position]

          holder.linkName.text = abaLinkItem.Name

          if (darkMode)
               rowFG?.setBackgroundColor(Color.GRAY)

          for (i in abaLinksTypes.indices) {
               if (abaLinksTypes[i].ID == abaLinkItem.TypeID)
                    holder.linkInfo.text = abaLinksTypes[i].Name
          }
     }

     override fun getItemCount(): Int {
          return abaLinks.size
     }

     @JvmName("setDarkMode1")
     public fun setDarkMode(_darkMode: Boolean) {
          this.darkMode=_darkMode
     }

     inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
          val linkName: TextView = itemView.findViewById(R.id.link_name)
          val linkInfo: TextView = itemView.findViewById(R.id.link_info)
     }
}