package com.example.taskmanager.recyclerview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import org.w3c.dom.Text

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val usernameTextView = itemView.findViewById<TextView>(R.id.userNameRV)
    val role = itemView.findViewById<TextView>(R.id.userroleRV)
}