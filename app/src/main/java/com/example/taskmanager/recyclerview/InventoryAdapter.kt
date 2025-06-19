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

        holder.arrivalDate.text = "${holder.itemView.context.getString(R.string.arrival)}: ${formattedArrival}"
        holder.count.text = "${holder.itemView.context.getString(R.string.count)}: ${equipment.count}"
        holder.category.text = "${holder.itemView.context.getString(R.string.category)}: ${equipment.category}"
        holder.equipmentName.text = "${holder.itemView.context.getString(R.string.model)}: ${equipment.equipmentName}"
        holder.userName.text = "${holder.itemView.context.getString(R.string.receiver)}: ${equipment.receiver}"
        holder.sender.text = "${holder.itemView.context.getString(R.string.sender)}: ${equipment.sender}"
        holder.itemStatus.text = "${holder.itemView.context.getString(R.string.status)}: ${equipment.status}"

        when(equipment.status){
            "New" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_green))
            "Used" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.light_yellow))
            "Out of order" -> holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.expired_red))
        }

        if (equipment.archivedAt != null) {
            val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(equipment.archivedAt))
            holder.archivedDate.text = "${holder.itemView.context.getString(R.string.archived)}: ${formatted}"
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