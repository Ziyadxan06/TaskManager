package com.example.taskmanager.recyclerview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import org.w3c.dom.Text

class InventoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val equipmentName = itemView.findViewById<TextView>(R.id.inventarynameView)
    val category = itemView.findViewById<TextView>(R.id.categoryView)
    val arrivalDate = itemView.findViewById<TextView>(R.id.arrivalView)
    val count = itemView.findViewById<TextView>(R.id.countView)
    val userName = itemView.findViewById<TextView>(R.id.usernameView)
    val sender = itemView.findViewById<TextView>(R.id.senderView)
    val archivedDate = itemView.findViewById<TextView>(R.id.archivedView)
    val itemStatus = itemView.findViewById<TextView>(R.id.itemStatusView)
}