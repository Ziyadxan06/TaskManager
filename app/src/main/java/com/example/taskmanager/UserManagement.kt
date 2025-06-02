package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentUserManagementBinding
import com.example.taskmanager.recyclerview.TasksAdapter
import com.example.taskmanager.recyclerview.TasksModel
import com.example.taskmanager.recyclerview.UserAdapter
import com.example.taskmanager.recyclerview.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.nio.file.attribute.UserPrincipal

class UserManagement : Fragment() {

    private var _binding: FragmentUserManagementBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: ArrayList<UserModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userList = ArrayList()

        recyclerView = binding.userRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        userAdapter = UserAdapter(userList) { selectedItem ->
            val action = UserManagementDirections.actionUserManagementToUserDetailsDialogFragment(selectedItem.uid ?: "")
            findNavController().navigate(action)
        }
        binding.userRecyclerView.adapter = userAdapter

        binding.toolbarUserManagement.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarUserManagement.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        getData()

        parentFragmentManager.setFragmentResultListener("userRoleUpdated", viewLifecycleOwner) { _, bundle ->
            val updatedUserId = bundle.getString("updatedUserId")
            if (!updatedUserId.isNullOrBlank()) {
                refreshUser(updatedUserId)
            }
        }
    }

    fun getData(){
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { documents ->
                userList.clear()
                for(document in documents){
                    if (document.id != currentUserId) {
                        val user = document.toObject(UserModel::class.java)
                        val userWithId = user.copy(uid = document.id)
                        userList.add(userWithId)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }
    }

    private fun refreshUser(userId: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val updatedUser = document.toObject(UserModel::class.java)
                val index = userList.indexOfFirst { it.uid == userId }

                if (updatedUser != null && index != -1) {
                    userList[index] = updatedUser.copy(uid = document.id)
                    userAdapter.notifyItemChanged(index)
                }
            }
    }
}