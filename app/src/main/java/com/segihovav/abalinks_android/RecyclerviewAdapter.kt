package com.segihovav.abalinks_android

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerviewAdapter internal constructor(private val mContext: Context,private val abaLinks: MutableList<AbaLink>,private val abaLinksTypes: MutableList<AbaLinkType>) : RecyclerView.Adapter<RecyclerviewAdapter.MyViewHolder>() {
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
          val view: View = LayoutInflater.from(mContext).inflate(R.layout.link_item, parent, false)
          return MyViewHolder(view)
     }

     override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
          val abaLinkItem: AbaLink = abaLinks[position]

          /*if (abaLinkItem.URL != null && (abaLinkItem.URL.toString().contains("http") || abaLinkItem.URL.toString().contains("https"))) {
               holder.linkName.text = Html.fromHtml("<A HREF=${abaLinks[position].URL}>${abaLinks[position].Name}</A>", HtmlCompat.FROM_HTML_MODE_LEGACY)
               holder.linkName.movementMethod = LinkMovementMethod.getInstance()
          } else {
              holder.linkName.text = abaLinkItem.Name
          }*/
          holder.linkName.text = abaLinkItem.Name

          for (i in abaLinksTypes.indices) {
               if (abaLinksTypes[i].ID == abaLinkItem.TypeID)
                    holder.linkInfo.text = abaLinksTypes[i].Name
          }
     }

     override fun getItemCount(): Int {
          return abaLinks.size
     }

     inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
          val linkName: TextView = itemView.findViewById(R.id.link_name)
          val linkInfo: TextView = itemView.findViewById(R.id.link_info)
     }
}