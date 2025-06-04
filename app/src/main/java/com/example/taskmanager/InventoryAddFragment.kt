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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.databinding.FragmentInventoryAddBinding
import com.example.taskmanager.databinding.FragmentInventoryListBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InventoryAddFragment : Fragment() {

    private var _binding: FragmentInventoryAddBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResulLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null
    private lateinit var equipmentName: String
    private lateinit var category: String
    private lateinit var count: String
    private lateinit var status: String
    private lateinit var location: String

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

        (activity as? AppCompatActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE

        registerLauncher()

        binding.toolbarAddItem.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarAddItem.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addImageView.setOnClickListener {
            getPermission()
        }

        binding.btnAddEquipment.setOnClickListener {
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
                    binding.addImageView.setImageURI(it)
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        equipmentName = binding.equipmentName.text.toString().trim()
        category = binding.equipmentType.text.toString().trim()
        count = binding.itemCount.text.toString().trim()
        status = binding.itemStatus.text.toString().trim()
        location = binding.location.text.toString().trim()


        if(equipmentName.isEmpty() || category.isEmpty() || count.isEmpty() || status.isEmpty()){
            Toast.makeText(requireContext(), "Butun xanalari doldurun", Toast.LENGTH_SHORT).show()
        }else{
            val inventoryItem = hashMapOf(
                "userId" to userId,
                "imageUrl" to imageUrl,
                "createdAt" to System.currentTimeMillis(),
                "equipmentName" to equipmentName,
                "category" to category,
                "count" to count,
                "itemstatus" to status,
                "isarchived" to false,
                "location" to location

            )

            FirebaseFirestore.getInstance().collection("inventory")
                .add(inventoryItem)
                .addOnSuccessListener { documentReference ->
                    val id = documentReference.id
                    documentReference.update("id", id)
                    Toast.makeText(requireContext(), "Inventory elementi əlavə edildi", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Xəta: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}