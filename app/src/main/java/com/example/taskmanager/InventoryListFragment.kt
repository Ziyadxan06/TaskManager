package com.example.taskmanager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentInventoryListBinding
import com.example.taskmanager.recyclerview.InventoryAdapter
import com.example.taskmanager.recyclerview.InventoryModel
import com.google.firebase.auth.FirebaseAuth
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
            val action = InventoryListFragmentDirections.actionInventoryListFragmentToInventoryDetailsDialogFragment(selectedItem.id)
            findNavController().navigate(action)
        }
        binding.adminInventoryRV.adapter = inventoryAdapter
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "staff"
                setupFilterSpinner(role, currentUser)
            }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_inventoryListFragment_to_inventoryAddFragment)
        }



    }

    private fun setupFilterSpinner(role: String, userId: String) {

        val options = if(role == "admin" || role == "superadmin"){
            arrayOf("All Items", "My Items", "By User", "By Arrival Date")
        }else{
            arrayOf("All Items", "By Arrival Date")
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
        binding.filterSpinner.adapter = adapter

        requireActivity().addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.optionsmenu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId){
                    R.id.menu_settings -> { findNavController().navigate(R.id.action_inventoryListFragment_to_settingsFragment)
                        true
                    }
                    else -> {false}
                }
            }
        }, viewLifecycleOwner )

        val toolbar = binding.inventoryToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)


        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(role == "admin" || role == "superadmin"){
                    when (position) {
                        0 -> fetchAllInventory(role, userId)
                        1 -> fetchUserInventory(userId)
                        2 -> showEmailInputDialog()
                        3 -> showArrivalDatePicker(role, userId)
                    }
                }else{
                    when(position){
                        0 -> fetchAllInventory(role, userId)
                        1 -> showArrivalDatePicker(role, userId)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    fun fetchAllInventory(role: String, userId: String) {
        equipmentList.clear()
        inventoryAdapter.notifyDataSetChanged()
        val query = if(role == "admin" || role == "superadmin"){
            FirebaseFirestore.getInstance().collection("inventory")
        }else{
            FirebaseFirestore.getInstance().collection("inventory").whereEqualTo("userId", userId)
        }
        query.get().addOnSuccessListener { documents ->
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

    fun fetchUserInventory(userId: String) {
        equipmentList.clear()
        inventoryAdapter.notifyDataSetChanged()

        FirebaseFirestore.getInstance().collection("inventory")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
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

    fun fetchInventoryByEmail(userEmail: String) {
        FirebaseFirestore.getInstance().collection("inventory")
            .whereEqualTo("userEmail", userEmail)
            .get()
            .addOnSuccessListener {documents ->
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

                inventoryAdapter.notifyDataSetChanged()}
    }

    private fun showEmailInputDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter user email"

        AlertDialog.Builder(requireContext())
            .setTitle("Filter by Email")
            .setView(input)
            .setPositiveButton("OK") { dialog, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    equipmentList.clear()
                    inventoryAdapter.notifyDataSetChanged()
                    fetchInventoryByEmail(email)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun showArrivalDatePicker(role: String, userId: String) {
        val calendar = java.util.Calendar.getInstance()

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val startOfDay = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 23, 59, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.timeInMillis

            equipmentList.clear()
            inventoryAdapter.notifyDataSetChanged()
            fetchInventoryByDateRange(startOfDay, endOfDay, role, userId)

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        datePicker.setTitle("Select Arrival Date")
        datePicker.show()
    }


    fun fetchInventoryByDateRange(start: Long, end: Long, role: String, userId: String) {
        val query = if(role == "admin" || role == "superadmin"){
            FirebaseFirestore.getInstance().collection("inventory")
        }else{
            FirebaseFirestore.getInstance().collection("inventory").whereEqualTo("userId", userId)
        }
        query.whereGreaterThanOrEqualTo("createdAt", start)
            .whereLessThanOrEqualTo("createdAt", end)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("equipmentName") ?: ""
                    val category = document.getString("category") ?: ""
                    val imageUri = document.getString("imageUrl") ?: ""
                    val macAddress = document.getString("MACaddress") ?: ""
                    val ipAddress = document.getString("IPaddress") ?: ""
                    val arrival = document.getLong("createdAt") ?: 0L

                    val equipment = InventoryModel(id, name, category, macAddress, ipAddress, imageUri, arrival)
                    equipmentList.add(equipment)
                }
                inventoryAdapter.notifyDataSetChanged()
            }
    }
}