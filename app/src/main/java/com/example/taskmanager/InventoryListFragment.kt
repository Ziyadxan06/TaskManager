package com.example.taskmanager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentInventoryListBinding
import com.example.taskmanager.recyclerview.InventoryAdapter
import com.example.taskmanager.recyclerview.InventoryModel
import com.example.taskmanager.recyclerview.UserModel
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

        archive()

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

    }

    private fun setupFilterSpinner(role: String, userId: String) {

        val options = arrayOf("All Items", "My Items", "By User", "By Arrival Date")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            options
        ) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.spinner_dropdown_bg))
                return view
            }

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.WHITE)
                return view
            }
        }

        binding.filterSpinner.adapter = adapter

        val toolbar = binding.inventoryToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)


        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> fetchAllInventory()
                        1 -> fetchUserInventory(userId)
                        2 -> showEmailInputDialog()
                        3 -> showArrivalDatePicker()
                    }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    fun fetchAllInventory() {
        equipmentList.clear()
        inventoryAdapter.notifyDataSetChanged()

        FirebaseFirestore.getInstance().collection("inventory").whereEqualTo("isarchived", false).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.get("id") as? String ?: ""
                    val name = document.get("equipmentName") as? String ?: ""
                    val category = document.get("category") as? String ?: ""
                    val imageUri = document.get("imageUrl") as? String ?: ""
                    val macAddress = document.get("count") as? String ?: ""
                    val ipAddress = document.get("itemstatus") as? String ?: ""
                    val arrival = document.get("createdAt") as? Long ?: 0L
                    val location = document.get("location") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val sender = document.get("sender") as? String ?: ""

                    val equipment = InventoryModel(id, name, category, macAddress, ipAddress, imageUri, arrival, location, userName, sender)
                    equipmentList.add(equipment)
                }

                inventoryAdapter.notifyDataSetChanged()
            }
    }

    fun fetchUserInventory(userId: String) {
        equipmentList.clear()
        inventoryAdapter.notifyDataSetChanged()

        FirebaseFirestore.getInstance().collection("inventory").whereEqualTo("isarchived", false)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                val id = document.get("id") as? String ?: ""
                val name = document.get("equipmentName") as? String ?: ""
                val category = document.get("category") as? String ?: ""
                val imageUri = document.get("imageUrl") as? String ?: ""
                val macAddress = document.get("count") as? String ?: ""
                val ipAddress = document.get("itemstatus") as? String ?: ""
                val arrival = document.get("createdAt") as? Long ?: 0L
                val location = document.get("location") as? String ?: ""
                val userName = document.get("userName") as? String ?: ""
                val sender = document.get("sender") as? String ?: ""



                val equipment = InventoryModel(id, name, category, macAddress, ipAddress, imageUri, arrival, location, userName, sender)
                equipmentList.add(equipment)
            }

                inventoryAdapter.notifyDataSetChanged()
            }
    }

    fun fetchInventoryByEmail(userId: String) {
        FirebaseFirestore.getInstance().collection("inventory")
            .whereEqualTo("isarchived", false)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener {documents ->
                for (document in documents) {
                    val id = document.get("id") as? String ?: ""
                    val name = document.get("equipmentName") as? String ?: ""
                    val category = document.get("category") as? String ?: ""
                    val imageUri = document.get("imageUrl") as? String ?: ""
                    val count = document.get("count") as? String ?: ""
                    val itemStatus = document.get("itemstatus") as? String ?: ""
                    val arrival = document.get("createdAt") as? Long ?: 0L
                    val location = document.get("location") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val sender = document.get("sender") as? String ?: ""



                    val equipment = InventoryModel(id, name, category, count, itemStatus, imageUri, arrival, location, userName, sender)
                    equipmentList.add(equipment)
                }

                inventoryAdapter.notifyDataSetChanged()}
    }

    private fun showEmailInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_filter, null)
        val autoCompleteTextView = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteUser)

        autoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.requestFocus()

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userList = mutableListOf<UserModel>()
                val displayList = mutableListOf<String>()

                for (doc in documents) {
                    val username = doc.getString("username") ?: ""
                    val email = doc.getString("useremail") ?: ""
                    val uId = doc.getString("id") ?: ""
                    if (email.isNotEmpty() && username.isNotEmpty()) {
                        val user = UserModel(uid = uId, username = username, useremail = email)
                        userList.add(user)
                        displayList.add("${user.username} (${user.useremail})")
                    }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList)
                autoCompleteTextView.setAdapter(adapter)


                autoCompleteTextView.postDelayed({
                    autoCompleteTextView.showDropDown()
                }, 200)

                AlertDialog.Builder(requireContext())
                    .setTitle("Filter by User")
                    .setView(dialogView)
                    .setPositiveButton("OK") { dialog, _ ->
                        val selectedText = autoCompleteTextView.text.toString().trim()
                        val selectedUser = userList.find {
                            "${it.username} (${it.useremail})" == selectedText
                        }

                        if (selectedUser != null) {
                            equipmentList.clear()
                            inventoryAdapter.notifyDataSetChanged()
                            fetchInventoryByEmail(selectedUser.uid ?: "")
                        } else {
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching users: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showArrivalDatePicker() {
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
            fetchInventoryByDateRange(startOfDay, endOfDay)

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        datePicker.setTitle("Select Arrival Date")
        datePicker.show()
    }


    fun fetchInventoryByDateRange(startMillis: Long, endMillis: Long) {
        val db = FirebaseFirestore.getInstance()

        db.collection("inventory")
            .whereEqualTo("isarchived", false)
            .whereGreaterThanOrEqualTo("createdAt", startMillis)
            .whereLessThanOrEqualTo("createdAt", endMillis)
            .get()
            .addOnSuccessListener { documents ->
                equipmentList.clear()
                for (document in documents) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("equipmentName") ?: ""
                    val category = document.getString("category") ?: ""
                    val imageUri = document.getString("imageUrl") ?: ""
                    val macAddress = document.getString("count") ?: ""
                    val ipAddress = document.getString("itemstatus") ?: ""
                    val arrival = document.getLong("createdAt") ?: 0L
                    val location = document.getString("location") ?: ""
                    val userName = document.getString("userName") ?: ""
                    val sender = document.getString("sender") ?: ""

                    val equipment = InventoryModel(
                        id, name, category, macAddress, ipAddress,
                        imageUri, arrival, location, userName, sender
                    )
                    equipmentList.add(equipment)
                }

                inventoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Fetch failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun archive(){
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val swipedItem = equipmentList[position]

                FirebaseFirestore.getInstance()
                    .collection("inventory")
                    .document(swipedItem.id)
                    .update(
                        mapOf(
                            "isarchived" to true,
                            "archivedAt" to System.currentTimeMillis()
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "${swipedItem.equipmentName} arşivlendi", Toast.LENGTH_SHORT).show()
                        equipmentList.removeAt(position)
                        inventoryAdapter.notifyItemRemoved(position)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Arşivleme başarısız: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                        inventoryAdapter.notifyItemChanged(position)
                    }
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.adminInventoryRV)
    }
}