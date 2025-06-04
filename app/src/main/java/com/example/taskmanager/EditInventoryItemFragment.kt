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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.taskmanager.databinding.FragmentEditInventoryItemBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class EditInventoryItemFragment : Fragment() {

    private var _binding: FragmentEditInventoryItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResulLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private val args: EditInventoryItemFragmentArgs by navArgs()
    private lateinit var defaultUri: String
    private lateinit var locationAdapterEdit: ArrayAdapter<String>
    private lateinit var statusAdapterEdit: ArrayAdapter<String>

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
                    val name = document.getString("equipmentName") ?: "-"
                    val category = document.getString("category") ?: "-"
                    val count = document.getString("count") ?: "-"
                    val status = document.getString("itemstatus") ?: "-"
                    val location = document.getString("location") ?: ""
                    defaultUri = document.getString("imageUrl") ?: ""

                    binding.editequipmentName.setText(name)
                    binding.editequipmentType.setText(category)
                    binding.editCount.setText(count)
                    binding.editStatus.setText(status, false)
                    binding.editLocation.setText(location, false)
                }
            }

        binding.editLocation.setOnClickListener {
            binding.editLocation.showDropDown()
        }

        binding.editStatus.setOnClickListener {
            binding.editStatus.showDropDown()
        }

        binding.editImageView.setOnClickListener {
            getPermission()
        }

        binding.btnEquipEdit.setOnClickListener {
            selectedImageUri?.let {
                updateData(it.toString())
            } ?: run {
                updateData(defaultUri)
            }
        }
    }

    private fun getPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    binding.root,
                    "Permission needed for gallery",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Give Permission") {
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
        activityResulLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageData = result.data?.data
                    imageData?.let {
                        selectedImageUri = it
                        binding.editImageView.setImageURI(it)
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    openGallery()
                } else {
                    Snackbar.make(
                        binding.root,
                        "Permission denied. Cannot access gallery.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun openGallery() {
        val intentToGallery =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResulLauncher.launch(intentToGallery)
    }

    private fun updateData(imageUrl: String) {
        val updatedName = binding.editequipmentName.text.toString().trim()
        val updatedCategory = binding.editequipmentType.text.toString().trim()
        val updatedCount = binding.editCount.text.toString().trim()
        val updatedStatus = binding.editStatus.text.toString().trim()
        val updatedLocation = binding.editLocation.text.toString().trim()

        val updatedData = hashMapOf<String, Any>(
            "equipmentName" to updatedName,
            "category" to updatedCategory,
            "count" to updatedCount,
            "itemstatus" to updatedStatus,
            "location" to updatedLocation,
            "imageUrl" to imageUrl
        )

        FirebaseFirestore.getInstance()
            .collection("inventory")
            .document(args.itemId)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Data Updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Yeniləmə xətası: ${it.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
