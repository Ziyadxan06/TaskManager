package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.databinding.FragmentOptionsMenuAdminBinding
import com.example.taskmanager.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        binding.useridAccount.setText(user?.uid ?: "-")
        binding.useremailAccount.setText(user?.email ?: "-")

        uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if(document.exists()){
                    val username = document.getString("username")
                    val role = document.getString("role")

                    binding.usernameAccount.setText(username)
                    binding.roleAccount.setText(role)
                }else {
                    Toast.makeText(context, "İstifadəçi tapılmadı", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Xəta: ${it.message}", Toast.LENGTH_LONG).show()
            }

        binding.btnupdateName.setOnClickListener {
            updateName()
        }
    }

    fun updateName(){
        val updatedName = binding.usernameAccount.text.toString().trim()

        val updateMap = hashMapOf<String, Any>(
            "username" to updatedName
        )

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Username Yenilendi", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Xəta baş verdi: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

}