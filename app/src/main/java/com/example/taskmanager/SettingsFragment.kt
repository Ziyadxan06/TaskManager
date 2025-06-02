package com.example.taskmanager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.taskmanager.databinding.FragmentSettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.settingsProgressBar.visibility = View.VISIBLE
        binding.cardAccount.visibility = View.GONE
        binding.cardUserManagement.visibility = View.GONE
        binding.cardSignOut.visibility = View.GONE

        adminorstaff()
        auth = Firebase.auth

        binding.cardSignOut.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_settingsFragment_to_signInFragment)
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                findNavController().navigate(R.id.signInFragment, null, navOptions)
            }, 300)
        }

        binding.cardAccount.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
        }

        binding.cardUserManagement.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_userManagement)
        }
    }

    fun adminorstaff(){
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (FirebaseAuth.getInstance().currentUser == null) return

        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                binding.settingsProgressBar.visibility = View.GONE
                binding.cardAccount.visibility = View.VISIBLE
                binding.cardSignOut.visibility = View.VISIBLE
                if (role == "admin" || role == "superadmin") {
                    binding.cardUserManagement.visibility = View.VISIBLE
                }
            }
    }
}