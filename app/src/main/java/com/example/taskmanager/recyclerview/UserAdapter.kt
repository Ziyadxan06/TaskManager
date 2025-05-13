package com.example.taskmanager.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R

class UserAdapter(val userList: ArrayList<UserModel>, private val onItemClick: (UserModel) -> Unit): RecyclerView.Adapter<UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerrowuser, parent, false)
        val userViewHolder = UserViewHolder(itemView)
        return userViewHolder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.usernameTextView.text = user.username
        holder.role.text = user.role

        holder.itemView.setOnClickListener{
            onItemClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}