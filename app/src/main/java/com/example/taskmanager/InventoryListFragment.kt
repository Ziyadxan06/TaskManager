package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentInventoryListBinding
import com.example.taskmanager.recyclerview.InventoryAdapter
import com.example.taskmanager.recyclerview.InventoryModel
import com.google.firebase.firestore.FirebaseFirestore

class InventoryListFragment : Fragment() {

    private var _binding: FragmentInventoryListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var equipmentList: ArrayList<InventoryModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryListBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        equipmentList = ArrayList()
        recyclerView = binding.adminInventoryRV
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        inventoryAdapter = InventoryAdapter(equipmentList) { selectedItem ->

        }
        binding.adminInventoryRV.adapter = inventoryAdapter

        getData()

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_inventoryListFragment_to_inventoryAddFragment)
        }
    }

    private fun getData() {
        FirebaseFirestore.getInstance().collection("inventory").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else if (value != null && !value.isEmpty) {

                val documents = value.documents

                for (document in documents) {
                    val id = document.get("id") as? String ?: ""
                    val name = document.get("equipmentName") as? String ?: ""
                    val category = document.get("category") as? String ?: ""
                    val imageUri = document.get("imageUrl") as? String ?: ""
                    val macAddress = document.get("MACaddress") as? String ?: ""
                    val ipAddress = document.get("IPaddress") as? String ?: ""
                    val arrival = document.get("createdAt") as? Long ?: 0L

                    val equipment = InventoryModel(id, name, category, macAddress, ipAddress, imageUri, arrival)
                    equipmentList.add(equipment)
                }

                inventoryAdapter.notifyDataSetChanged()
            }
        }
    }
}