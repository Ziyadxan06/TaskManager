package com.example.taskmanager.recyclerview

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import java.util.Date
import java.util.Locale

class InventoryAdapter(val equipmentList: ArrayList<InventoryModel>, private val onClickItem: (InventoryModel) -> Unit) : RecyclerView.Adapter<InventoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerrowinventary, parent, false)
        val inventoryViewHolder = InventoryViewHolder(itemView)
        return inventoryViewHolder
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val equipment = equipmentList[position]

        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val formattedArrival = formatter.format(Date(equipment.createdAt))

        holder.arrivalDate.text = "Arrival Date: ${formattedArrival}"
        holder.count.text = "Count: ${equipment.count}"
        holder.category.text = "Category: ${equipment.category}"
        holder.equipmentName.text = "Model: ${equipment.equipmentName}"
        holder.userName.text = "Receiver: ${equipment.receiver}"

        holder.itemView.setOnClickListener {
            onClickItem(equipment)
        }
    }

    override fun getItemCount(): Int {
        return equipmentList.size
    }
}