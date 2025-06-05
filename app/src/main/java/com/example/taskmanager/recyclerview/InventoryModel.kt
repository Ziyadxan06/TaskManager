package com.example.taskmanager.recyclerview

data class InventoryModel(val id: String, val equipmentName: String, val category: String, val count: String, val status: String, val imageUri: String, val createdAt: Long, val location: String, val receiver: String, val sender: String) {
}