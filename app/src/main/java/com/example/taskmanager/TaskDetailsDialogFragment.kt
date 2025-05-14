package com.example.taskmanager

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.databinding.FragmentTaskDetailsDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class TaskDetailsDialogFragment : DialogFragment() {

    private var _binding: FragmentTaskDetailsDialogBinding? = null
    private val binding get() = _binding!!

    private val args: TaskDetailsDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTaskDetailsDialogBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskId = args.taskId

        FirebaseFirestore.getInstance().collection("tasks")
            .document(taskId)
            .get()
            .addOnSuccessListener{ document ->
                binding.tasknameTextView.text = document.getString("name") ?: "-"
                binding.priorityTextView.text = document.getString("priority") ?: "-"
                binding.assignedtoTextView.text = document.getString("assignedTo") ?: "-"
                binding.statusTextView.text = document.getString("status") ?: "-"

                val deadlineMillis = document.getLong("deadline") ?: 0L
                val formatted = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(
                    Date(deadlineMillis)
                )
                binding.deadlineTextView.text = formatted

            }.addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserUid!!)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "admin" || role == "superadmin") {
                    binding.btnEdit.visibility = View.VISIBLE
                }
            }

        binding.btnEdit.setOnClickListener {
            val action = TaskDetailsDialogFragmentDirections.actionTaskDetailsDialogFragmentToEditTaskFragment(taskId)
            findNavController().navigate(action)
        }
    }
}