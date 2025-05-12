package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return (binding.root)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        binding.linkviewSignin.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.btnSignin.setOnClickListener {
            signin()
        }

        binding.linkviewReset.setOnClickListener {
            resetPassword()
        }

        if(currentUser != null){

        }
    }

    fun signin(){

        email = binding.signinEmail.text.toString()
        password = binding.signinPassword.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(context, "Enter email and password", Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }
    }

    fun resetPassword(){
        email = binding.signinEmail.text.toString()
        password = binding.signinPassword.text.toString()

        if(email.isEmpty()){
            Toast.makeText(context, "Enter email and password", Toast.LENGTH_LONG).show()
        }else{
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(){task ->
                    if(task.isSuccessful){
                        Toast.makeText(context, "Reset linki emailinize gonderildi", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(context, "Xeta bas verdi", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}