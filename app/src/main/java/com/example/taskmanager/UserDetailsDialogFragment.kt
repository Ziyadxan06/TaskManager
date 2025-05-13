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

        getUser()

        binding.userEmail.isSelected = true
        binding.userID.isSelected = true
        binding.userName.isSelected = true
    }

    fun getUser(){
        val userId = args.userId

        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener{ document ->
                binding.userID.text = document.getString("id") ?: "-"
                binding.userName.text = document.getString("username") ?: "-"
                binding.userEmail.text = document.getString("useremail") ?: "-"
                binding.userRole.text = document.getString("role") ?: "-"

            }.addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }
}