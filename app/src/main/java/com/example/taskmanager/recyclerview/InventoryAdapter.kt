package com.example.taskmanager.recyclerview

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        holder.sender.text = "Sender: ${equipment.sender}"

        when(equipment.status){
            "New" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_green))
            "Used" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_yellow))
            "Out of order" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.expired_red))
        }

        if (equipment.archivedAt != null) {
            val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(equipment.archivedAt))
            holder.archivedDate.text = "Archive Date: $formatted"
            holder.archivedDate.visibility = View.VISIBLE
        } else {
            holder.archivedDate.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            onClickItem(equipment)
        }
    }

    override fun getItemCount(): Int {
        return equipmentList.size
    }
}