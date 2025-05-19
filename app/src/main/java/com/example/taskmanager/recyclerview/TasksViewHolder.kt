package com.example.taskmanager.recyclerview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import org.w3c.dom.Text

class TasksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameTextView = itemView.findViewById<TextView>(R.id.taskName)
    val deadlineTextView = itemView.findViewById<TextView>(R.id.deadline)
    val status = itemView.findViewById<TextView>(R.id.statusView)
    val userName = itemView.findViewById<TextView>(R.id.userName)
}