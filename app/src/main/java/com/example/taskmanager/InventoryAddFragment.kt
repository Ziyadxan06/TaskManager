package com.example.taskmanager

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taskmanager.databinding.FragmentInventoryAddBinding
import com.example.taskmanager.databinding.FragmentInventoryListBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class InventoryAddFragment : Fragment() {

    private var _binding: FragmentInventoryAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResulLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private lateinit var equipmentName: String
    private lateinit var category: String
    private lateinit var macAddress: String
    private lateinit var ipAddress: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryAddBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        binding.addimageView.setOnClickListener {
            getPermission()
        }

        binding.btnequipAdd.setOnClickListener {
            selectedImageUri?.let { uri ->
                saveImageUriToFirestore(uri.toString()  )
            } ?: run {
                Toast.makeText(requireContext(), "Zəhmət olmasa şəkil seçin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(binding.root, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            openGallery()
        }
    }

    private fun registerLauncher(){
        activityResulLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == RESULT_OK){
                val imageData = result.data?.data
                imageData?.let {
                    selectedImageUri = it
                    binding.addimageView.setImageURI(it)
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if(granted){
                openGallery()
            }else{
                Snackbar.make(
                    binding.root,
                    "Permission denied. Cannot access gallery.",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openGallery(){
        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResulLauncher.launch(intentToGallery)
    }


    private fun saveImageUriToFirestore(imageUrl: String) {
        equipmentName = binding.equipmentName.text.toString().trim()
        category = binding.equipmentType.text.toString().trim()
        macAddress = binding.macAddress.text.toString().trim()
        ipAddress = binding.ipAddress.text.toString().trim()

        if(equipmentName.isEmpty() || category.isEmpty() || macAddress.isEmpty() || ipAddress.isEmpty()){
            Toast.makeText(requireContext(), "Butun xanalari doldurun", Toast.LENGTH_SHORT).show()
        }else{
            val inventoryItem = hashMapOf(
                "imageUrl" to imageUrl,
                "createdAt" to System.currentTimeMillis(),
                "equipmentName" to equipmentName,
                "category" to category,
                "MACaddress" to macAddress,
                "IPaddress" to ipAddress
            )

            FirebaseFirestore.getInstance().collection("inventory")
                .add(inventoryItem)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Inventory elementi əlavə edildi", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Xəta: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}