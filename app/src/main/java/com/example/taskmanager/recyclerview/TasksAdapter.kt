package com.example.taskmanager.recyclerview

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
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

        holder.nameTextView.text = task.taskName
        holder.deadlineTextView.text = formattedDeadline
        holder.status.text = task.status

        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

}