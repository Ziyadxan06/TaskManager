package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.databinding.FragmentSignUpBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var confirmPassword: String
    private lateinit var password: String
    private lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return (binding.root)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.linkviewSignup.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.btnSignup.setOnClickListener {
            signup()

        }
    }

    fun signup(){

        email = binding.signupEmail.text.toString()
        confirmPassword = binding.signupPassword2.text.toString()
        password = binding.signUpPassword.text.toString()
        userName = binding.signupUsername.text.toString()

        if(email == "" || password == "" || confirmPassword == "" || userName == ""){
            Toast.makeText(context, "Enter email and password", Toast.LENGTH_LONG).show()
        }else if(password != confirmPassword){
            Toast.makeText(context, "Təhlükəsizlik üçün şifrəni iki dəfə daxil etməlisiniz. Şifrələr uyğun gəlmir.", Toast.LENGTH_LONG).show()
        }
        else{
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    val db = FirebaseFirestore.getInstance()
                    val userData = hashMapOf(
                        "id" to uid,
                        "username" to userName,
                        "useremail" to email,
                        "role" to "staff"
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Hesab Yaradıldı", Toast.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_signUpFragment_to_adminTaskListFragment)
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .build()
                            findNavController().navigate(R.id.adminTaskListFragment, null, navOptions)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "DB Xətası: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                }
                .addOnFailureListener {
                    Toast.makeText(context, "Auth xətası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }
}