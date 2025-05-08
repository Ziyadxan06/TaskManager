package com.example.taskmanager.recyclerview

data class TasksModel(val id: String, val taskName: String, val deadline: Long, val assignedTo: String, val priority: String, val status: String)