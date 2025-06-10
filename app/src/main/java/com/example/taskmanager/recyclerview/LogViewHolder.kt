package com.example.taskmanager.recyclerview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R

class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val fieldChangedTextView = itemView.findViewById<TextView>(R.id.tvFieldChanged)
    val changedMetaTextView = itemView.findViewById<TextView>(R.id.tvChangedMeta)
    val logItemSnap = itemView.findViewById<TextView>(R.id.logItemSnapshot)
}