package com.example.taskmanager.recyclerview

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.taskmanager.R
import java.util.Date
import java.util.Locale

class TasksAdapter(val taskList: ArrayList<TasksModel>, private val onItemClick: (TasksModel) -> Unit) : RecyclerView.Adapter<TasksViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerrowtasks, parent, false)
        val tasksViewHolder = TasksViewHolder(itemView)
        return tasksViewHolder
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val task = taskList[position]

        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val formattedDeadline = formatter.format(Date(task.deadline))
        val container = holder.itemView.findViewById<LinearLayout>(R.id.recyclerView)
        val now = System.currentTimeMillis()
        val deadlineMillis = task.deadline

        holder.nameTextView.text = "Task Name: ${task.taskName}"
        holder.deadlineTextView.text = "Deadline: ${formattedDeadline}"
        holder.status.text = "Status: ${task.status}"
        holder.userName.text = "User Name: ${task.userName}"


        if(deadlineMillis < now){
            container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.expired_red))
        }else if(deadlineMillis - now <= 3 * 24 * 60 * 60 * 1000){
            container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.deadline_yellow))
        } else when (task.status) {
            "Yeni" -> container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_blue))
            "Pending" -> container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_purple))
            "Done" -> container.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_green))
        }

        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

}