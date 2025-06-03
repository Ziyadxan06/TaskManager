package com.example.taskmanager

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentInventoryDetailsDialogBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class InventoryDetailsDialogFragment : DialogFragment() {

    private var _binding: FragmentInventoryDetailsDialogBinding? = null
    private val binding get() = _binding!!

    private val args: InventoryDetailsDialogFragmentArgs by navArgs()
    private lateinit var documentId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryDetailsDialogBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        documentId = args.equipmentId
        loadEquipmentDetails(documentId)

        binding.btnEditTask.setOnClickListener {
            val action = InventoryDetailsDialogFragmentDirections.actionInventoryDetailsDialogFragmentToEditInventoryItemFragment(documentId)
            findNavController().navigate(action)
        }
    }

    private fun loadEquipmentDetails(docId : String){
        FirebaseFirestore.getInstance().collection("inventory")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if(document != null && document.exists()){
                    val name = document.getString("equipmentName") ?: "-"
                    val category = document.getString("category") ?: "-"
                    val mac = document.getString("count") ?: "-"
                    val ip = document.getString("itemstatus") ?: "-"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val createdAt = document.getLong("createdAt") ?: 0L

                    val formattedDate = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                        .format(Date(createdAt))

                    binding.equipmentnameTextView.text = name
                    binding.categoryTextView.text = category
                    binding.macaddressTextView.text = mac
                    binding.ipaddressTextView.text = ip
                    binding.arrival.text = formattedDate

                    Glide.with(requireContext())
                        .load(imageUrl)
                        .into(binding.equipmentImage)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }
}