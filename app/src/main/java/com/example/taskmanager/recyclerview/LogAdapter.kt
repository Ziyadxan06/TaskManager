package com.example.taskmanager.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log

class LogAdapter(val logList: ArrayList<LogModel>) : RecyclerView.Adapter<LogViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerrowlog, parent, false)
        val logViewHolder = LogViewHolder(itemView)
        return logViewHolder
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]

        holder.fieldChangedTextView.text = when (log.fieldChanged) {
            "status" -> "✅ Status changed: '${log.oldValue}' ➝ '${log.newValue}'"
            "location" -> "📍 Location changed: '${log.oldValue}' ➝ '${log.newValue}'"
            else -> "${log.fieldChanged} changed: '${log.oldValue}' ➝ '${log.newValue}'"
        }

        holder.changedMetaTextView.text = "${formatDate(log.changedAt)} | by ${log.changedByName ?: "Unknown"}"
    }

    private fun formatDate(date: Date?): String {
        return if (date != null) {
            val format = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            format.format(date)
        } else {
            "Unknown time"
        }
    }

    override fun getItemCount(): Int {
        return logList.size
    }
}