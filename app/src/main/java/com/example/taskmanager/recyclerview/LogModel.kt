package com.example.taskmanager.recyclerview

import java.util.Date

data class LogModel(val id: String, val fieldChanged: String, val oldValue: String, val newValue: String, val changedBy: String, val changedAt: Date?) {
}