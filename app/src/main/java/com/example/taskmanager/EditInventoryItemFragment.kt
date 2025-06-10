package com.example.taskmanager

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.taskmanager.databinding.FragmentEditInventoryItemBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EditInventoryItemFragment : Fragment() {

    private var _binding: FragmentEditInventoryItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResulLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private val args: EditInventoryItemFragmentArgs by navArgs()
    private lateinit var defaultUri: String
    private lateinit var status: String
    private lateinit var location: String
    private lateinit var count: String
    private lateinit var name: String
    private lateinit var category: String
    private lateinit var locationAdapterEdit: ArrayAdapter<String>
    private lateinit var statusAdapterEdit: ArrayAdapter<String>
    private lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditInventoryItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()
        getCurrentUserName()

        binding.toolbarEditItem.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarEditItem.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val statusList = listOf("New", "Used", "Out of order")
        val locationList = listOf("A1", "A2", "A3")

        statusAdapterEdit = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, statusList)
        locationAdapterEdit = ArrayAdapter(requireContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, locationList)

        binding.editStatus.setAdapter(statusAdapterEdit)
        binding.editLocation.setAdapter(locationAdapterEdit)

        val docId = args.itemId
        FirebaseFirestore.getInstance().collection("inventory")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    name = document.getString("equipmentName") ?: "-"
                    category = document.getString("category") ?: "-"
                    count = document.getString("count") ?: "-"
                    status = document.getString("itemstatus") ?: "-"
                    location = document.getString("location") ?: ""
                    defaultUri = document.getString("imageUrl") ?: ""

                    binding.editequipmentName.setText(name)
                    binding.editequipmentType.setText(category)
                    binding.editCount.setText(count)
                    binding.editStatus.setText(status, false)
                    binding.editLocation.setText(location, false)
                }
            }

        binding.editLocation.setOnClickListener { binding.editLocation.showDropDown() }
        binding.editStatus.setOnClickListener { binding.editStatus.showDropDown() }
        binding.editImageView.setOnClickListener { getPermission() }

        binding.btnEquipEdit.setOnClickListener {
            selectedImageUri?.let {
                updateData(it.toString(), status, count, location, name, category)
            } ?: run {
                updateData(defaultUri, status, count, location, name, category)
            }
        }
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(binding.root, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            openGallery()
        }
    }

    private fun registerLauncher() {
        activityResulLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageData = result.data?.data
                imageData?.let {
                    selectedImageUri = it
                    binding.editImageView.setImageURI(it)
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openGallery()
            else Snackbar.make(binding.root, "Permission denied. Cannot access gallery.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResulLauncher.launch(intentToGallery)
    }

    private fun updateData(imageUrl: String, originalStatus: String, originalCount: String, originalLocation: String, originalName: String, originalCategory: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updatedName = binding.editequipmentName.text.toString().trim()
        val updatedCategory = binding.editequipmentType.text.toString().trim()
        val updatedCount = binding.editCount.text.toString().trim()
        val updatedStatus = binding.editStatus.text.toString().trim()
        val updatedLocation = binding.editLocation.text.toString().trim()

        val isCountReduced = updatedCount < originalCount
        val isCountIncreased = updatedCount > originalCount
        val isCountChanged = updatedCount.toIntOrNull() != originalCount.toIntOrNull()
        val isNameChanged = updatedName != originalName
        val isCategoryChanged = updatedCategory != originalCategory
        val isStatusChanged = updatedStatus != originalStatus
        val isLocationChanged = updatedLocation != originalLocation
        val requiresSplit = isCountReduced && (isStatusChanged || isLocationChanged)
        val warnAndSuggestSplit = isCountIncreased && (isStatusChanged || isLocationChanged)

        if (requiresSplit) {
            val countDifference = originalCount.toInt() - updatedCount.toInt()

            val addNewMap = mapOf(
                "equipmentName" to updatedName,
                "category" to updatedCategory,
                "count" to updatedCount,
                "itemstatus" to updatedStatus,
                "location" to updatedLocation,
                "imageUrl" to imageUrl
            )

            FirebaseFirestore.getInstance().collection("inventory")
                .document(args.itemId)
                .update(addNewMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_LONG).show()
                    if (isStatusChanged) logChange(args.itemId, "status", originalStatus, updatedStatus, userId, userName)
                    if (isLocationChanged) logChange(args.itemId, "location", originalLocation, updatedLocation, userId, userName)
                    if (isCountChanged) logChange(args.itemId, "count", originalCount, updatedCount, userId, userName)
                    if (isNameChanged) logChange(args.itemId, "name", originalName, updatedName, userId, userName)
                    if (isCategoryChanged) logChange(args.itemId, "category", originalCategory, updatedCategory, userId, userName)
                }

            val newItem = hashMapOf(
                "equipmentName" to updatedName,
                "category" to updatedCategory,
                "count" to countDifference.toString(),
                "itemstatus" to originalStatus,
                "location" to originalLocation,
                "imageUrl" to imageUrl,
                "createdAt" to System.currentTimeMillis(),
                "isarchived" to false,
                "userId" to userId,
                "userName" to userName
            )

            FirebaseFirestore.getInstance().collection("inventory")
                .add(newItem)
                .addOnSuccessListener { documentReference ->
                    documentReference.update("id", documentReference.id)
                    findNavController().popBackStack()
                }
        } else if (warnAndSuggestSplit) {
            AlertDialog.Builder(requireContext())
                .setTitle("Warning")
                .setMessage("You are increasing the item count while changing its status or location. It's recommended to add these as a new item instead.")
                .setPositiveButton("Add as new item") { _, _ ->
                    createNewItemOnly(imageUrl, originalStatus, originalCount)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        } else {
            val updateMap = mapOf(
                "equipmentName" to updatedName,
                "category" to updatedCategory,
                "count" to updatedCount,
                "itemstatus" to updatedStatus,
                "location" to updatedLocation,
                "imageUrl" to imageUrl
            )

            FirebaseFirestore.getInstance().collection("inventory")
                .document(args.itemId)
                .update(updateMap)
                .addOnSuccessListener {
                    if (isStatusChanged) logChange(args.itemId, "status", originalStatus, updatedStatus, userId, userName)
                    if (isLocationChanged) logChange(args.itemId, "location", originalLocation, updatedLocation, userId, userName)
                    if (isCountChanged) logChange(args.itemId, "count", originalCount, updatedCount, userId, userName)
                    if (isNameChanged) logChange(args.itemId, "name", originalName, updatedName, userId, userName)
                    if (isCategoryChanged) logChange(args.itemId, "category", originalCategory, updatedCategory, userId, userName)

                    Toast.makeText(requireContext(), "Data Updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }
    }

    private fun createNewItemOnly(imageUrl: String, originalStatus: String, originalCount: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val updatedName = binding.editequipmentName.text.toString().trim()
        val updatedCategory = binding.editequipmentType.text.toString().trim()
        val updatedCount = binding.editCount.text.toString().trim()
        val updatedStatus = binding.editStatus.text.toString().trim()
        val updatedLocation = binding.editLocation.text.toString().trim()

        val countDifference = updatedCount.toInt() - originalCount.toInt()

        val newItem = hashMapOf(
            "equipmentName" to updatedName,
            "category" to updatedCategory,
            "count" to countDifference.toString(),
            "itemstatus" to updatedStatus,
            "location" to updatedLocation,
            "imageUrl" to imageUrl,
            "createdAt" to System.currentTimeMillis(),
            "isarchived" to false,
            "userId" to userId,
            "userName" to userName
        )

        FirebaseFirestore.getInstance().collection("inventory")
            .add(newItem)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                findNavController().popBackStack()
            }
    }

    private fun logChange(inventoryId: String, field: String, oldValue: String, newValue: String, userId: String, userName: String) {
        FirebaseFirestore.getInstance().collection("inventory")
            .document(inventoryId)
            .get()
            .addOnSuccessListener { document ->
                val itemName = document.getString("equipmentName") ?: "-"
                val itemLoc = document.getString("location") ?: "-"
                val itemStatus = document.getString("itemstatus") ?: "-"

                val log = hashMapOf(
                    "inventoryId" to inventoryId,
                    "fieldChanged" to field,
                    "oldValue" to oldValue,
                    "newValue" to newValue,
                    "changedBy" to userId,
                    "changedByName" to userName,
                    "changedAt" to FieldValue.serverTimestamp(),
                    "itemSnapshot" to "$itemName - $itemLoc - $itemStatus"
                )

                FirebaseFirestore.getInstance()
                    .collection("inventory_logs")
                    .add(log)
            }
    }


    private fun getCurrentUserName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("username") ?: ""
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}