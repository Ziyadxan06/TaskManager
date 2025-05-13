package com.example.taskmanager

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.databinding.FragmentUserDetailsDialogBinding
import com.example.taskmanager.databinding.FragmentUserManagementBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class UserDetailsDialogFragment : DialogFragment() {

    private var _binding: FragmentUserDetailsDialogBinding? = null
    private val binding get() = _binding!!

    private val args: UserDetailsDialogFragmentArgs by navArgs()
    lateinit var userId: String
    private var shouldHandleChipChange = false
    private lateinit var currentRole: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserDetailsDialogBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = args.userId

        getUser()
        setupChipListeners()
        shouldHandleChipChange = true

        binding.userEmail.isSelected = true
        binding.userID.isSelected = true
        binding.userName.isSelected = true
    }

    private fun getUser(){
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener{ document ->
                val role = document.getString("role") ?: ""
                binding.userID.text = document.getString("id") ?: "-"
                binding.userName.text = document.getString("username") ?: "-"
                binding.userEmail.text = document.getString("useremail") ?: "-"
                binding.userRole.text = role

                currentRole = role.lowercase()

                shouldHandleChipChange = false

                when (currentRole) {
                    "admin" -> binding.chipAdmin.isChecked = true
                    "staff" -> binding.chipStaff.isChecked = true
                }

                shouldHandleChipChange = true

            }.addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun setupChipListeners(){
        binding.roleChipGroup.setOnCheckedStateChangeListener{group, checkedIds ->
            if (!shouldHandleChipChange) return@setOnCheckedStateChangeListener

            if (checkedIds.isNotEmpty()) {
                val checkedId = checkedIds[0]
                val newRole = when (checkedId) {
                    R.id.chipAdmin -> "admin"
                    R.id.chipStaff -> "staff"
                    else -> return@setOnCheckedStateChangeListener
                }

                if (newRole != currentRole) {
                    updateUserRole(userId, newRole)
                    currentRole = newRole
                }
            }
        }
    }

    private fun updateUserRole(userId: String, role: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("role", role)
            .addOnSuccessListener {
                Toast.makeText(context, "Role updated to $role", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update role", Toast.LENGTH_SHORT).show()
            }
    }
}