package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.example.taskmanager.databinding.FragmentInventoryLogBinding
import com.example.taskmanager.recyclerview.LogAdapter
import com.example.taskmanager.recyclerview.LogModel
import com.google.firebase.firestore.FirebaseFirestore

class InventoryLogFragment : Fragment() {

    private var _binding: FragmentInventoryLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private lateinit var logList: ArrayList<LogModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryLogBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logList = ArrayList()
        recyclerView = binding.logRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        logAdapter = LogAdapter(logList)
        binding.logRecyclerView.adapter = logAdapter

        getData()

        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.optionsmenu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId){
                    R.id.menu_settings -> { findNavController().navigate(R.id.action_inventoryLogFragment_to_settingsFragment)
                        true
                    }
                    else -> {false}
                }
            }
        }, viewLifecycleOwner )
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarLogs)
    }

    private fun getData(){
        logList.clear()

        FirebaseFirestore.getInstance().collection("inventory_logs")
            .get()
            .addOnSuccessListener { documents ->
                for(doc in documents){
                    val id = doc.getString("inventoryId") ?: ""
                    val changedAt = doc.getDate("changedAt")
                    val changedBy = doc.getString("changedBy") ?: ""
                    val changedByName = doc.getString("changedByName") ?: ""
                    val fieldChanged = doc.getString("fieldChanged") ?: ""
                    val newValue = doc.getString("newValue") ?: ""
                    val oldValue = doc.getString("oldValue") ?: ""
                    val itemSnap = doc.getString("itemSnapshot") ?: ""

                    val log = LogModel(id, fieldChanged, oldValue, newValue, changedBy, changedByName, changedAt, itemSnap)
                    logList.add(log)
                }

                logAdapter.notifyDataSetChanged()
            }
    }
}