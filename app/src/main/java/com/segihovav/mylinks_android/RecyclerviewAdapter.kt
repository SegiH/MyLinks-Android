package com.segihovav.mylinks_android

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerviewAdapter internal constructor(private val mContext: Context, private val myLinks: MutableList<MyLink>, private val myLinksTypes: MutableList<MyLinkType>) : RecyclerView.Adapter<RecyclerviewAdapter.MyViewHolder>() {
     var darkMode: Boolean = false
     var rowFG: LinearLayout? = null
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
          val view: View = LayoutInflater.from(mContext).inflate(R.layout.link_item, parent, false)
          rowFG=view.findViewById(R.id.rowFG)
          return MyViewHolder(view)
     }

     override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
          val myLinkItem: MyLink = myLinks[position]

          val displaySize=60

          holder.linkName.text= if (myLinkItem.Name?.length!! > displaySize) myLinkItem.Name?.substring(0,displaySize) else myLinkItem.Name

          // Reduce font size
          holder.linkName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)

          if (darkMode)
               rowFG?.setBackgroundColor(Color.GRAY)

          for (i in myLinksTypes.indices) {
               if (myLinksTypes[i].ID == myLinkItem.TypeID)
                    holder.linkInfo.text = myLinksTypes[i].Name
          }
     }

     override fun getItemCount(): Int {
          return myLinks.size
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