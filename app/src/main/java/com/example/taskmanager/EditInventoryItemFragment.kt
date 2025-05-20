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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditInventoryItemBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerLauncher()

        var docId = args.itemId
        FirebaseFirestore.getInstance().collection("inventory")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if(document != null && document.exists()){
                    val name = document.getString("equipmentName") ?: "-"
                    val category = document.getString("category") ?: "-"
                    val mac = document.getString("MACaddress") ?: "-"
                    val ip = document.getString("IPaddress") ?: "-"
                    val imageUrl = document.getString("imageUrl") ?: ""
                    Log.d("EDIT_FRAGMENT", "Image URL: $imageUrl")


                    binding.editequipmentName.setText(name)
                    binding.editequipmentType.setText(category)
                    binding.editipAddress.setText(ip)
                    binding.editmacAddress.setText(mac)
                }
            }

        binding.editimageView.setOnClickListener {
            getPermission()
        }

        binding.btnequipEdit.setOnClickListener {
            selectedImageUri.let { uri ->
                updateData(uri.toString())
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
                    binding.editimageView.setImageURI(it)
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if(granted){
                openGallery()
            }else{
                Snackbar.make(binding.root, "Permission denied. Cannot access gallery", Snackbar.LENGTH_INDEFINITE).show()
            }
        }
    }

    private fun openGallery(){
        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResulLauncher.launch(intentToGallery)
    }

    private fun updateData(imageUrl: String){
        val updatedName = binding.editequipmentName.text.toString().trim()
        val updatedCategory = binding.editequipmentType.text.toString().trim()
        val updatedMac = binding.editmacAddress.text.toString().trim()
        val updatedIp = binding.editipAddress.text.toString().trim()

        val updatedData = hashMapOf<String, Any>(
            "equipmentName" to updatedName,
            "category" to updatedCategory,
            "MACaddress" to updatedMac,
            "IPaddress" to updatedIp,
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
                Toast.makeText(requireContext(), "Yeniləmə xətası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

}