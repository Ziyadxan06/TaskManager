package com.example.taskmanager

import android.app.AlertDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
    var previousStatus: String = ""
    private var currentStatus: String =""
    private val currentTimeMillis = System.currentTimeMillis()

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
            .addSnapshotListener { snapshot, error ->
                if(error != null || snapshot == null) {
                    context?.let{
                        Toast.makeText(requireContext(), error?.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    return@addSnapshotListener
                }


                    binding.tasknameTextView.text = snapshot.getString("name") ?: "-"
                    binding.priorityTextView.text = snapshot.getString("priority") ?: "-"
                    binding.assignedtoTextView.text = snapshot.getString("assignedTo") ?: "-"
                    binding.statusTextView.text = snapshot.getString("status") ?: "-"
                    binding.usernameTextView.text = snapshot.getString("userName") ?: "-"

                    currentStatus = snapshot.getString("status") ?: "New"
                    val deadlineMillis = snapshot.getLong("deadline") ?: 0L
                    val formatted = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(
                        Date(deadlineMillis)
                    )
                    binding.deadlineTextView.text = formatted

                    if(currentStatus == "Done"){
                        restorePreviousSelection()
                        binding.statusRadioGroup.isEnabled = false
                        binding.radioDone.isEnabled = false
                        binding.radioInProgress.isEnabled = false
                        binding.radioDone.isChecked = true
                        binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
                        return@addSnapshotListener
                    }else if(currentStatus == "Pending"){
                        binding.radioInProgress.isChecked = true
                        context?.let {
                            binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_purple))
                        }
                    }else if(currentStatus == "Yeni" && deadlineMillis - currentTimeMillis > 3 * 24 * 60 * 60 * 1000){
                        context?.let {
                            binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
                        }
                    }else if(deadlineMillis - currentTimeMillis <= 3 * 24 * 60 * 60 * 1000 && currentStatus != "Overdue"){
                        context?.let {
                            binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deadline_yellow))
                        }
                    }else if(currentStatus == "Overdue"){
                        context?.let {
                            binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.expired_red))
                        }
                        binding.statusRadioGroup.isEnabled = false
                        binding.radioDone.isEnabled = false
                        binding.radioInProgress.isEnabled = false
                    }
            }

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserUid!!)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                if (role == "admin" || role == "superadmin") {
                    binding.btnEditTask.visibility = View.VISIBLE
                    binding.statusRadioGroup.visibility = View.GONE
                }
            }

        binding.btnEditTask .setOnClickListener {
            val action = TaskDetailsDialogFragmentDirections.actionTaskDetailsDialogFragmentToEditTaskFragment(taskId)
            findNavController().navigate(action)
        }

        loadTaskDetails(taskId)
    }

    private fun loadTaskDetails(taskId: String) {
        binding.statusRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            FirebaseFirestore.getInstance().collection("tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener { document ->
                    val status = document.getString("status") ?: "New"
                    val deadlineMillis = document.getLong("deadline") ?: 0L

                    previousStatus = status

                    val newStatus = when (checkedId) {
                        R.id.radioInProgress -> "Pending"
                        R.id.radioDone -> "Done"
                        else -> return@addOnSuccessListener
                    }

                    if (newStatus == "Done" && status != "Done") {
                        context.let {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Təsdiqləyin")
                                .setMessage("Bu tapşırığı 'tamamlandı' olaraq işarələmək istəyirsinizmi? Daha sonra dəyişmək mümkün olmayacaq.")
                                .setPositiveButton("Bəli") { _, _ ->
                                    updateTaskStatus(taskId, newStatus)
                                }
                                .setNegativeButton("Xeyr") { dialog, _ ->
                                    dialog.dismiss()
                                    restorePreviousSelection()
                                }
                                .show()
                        }
                    } else if(newStatus == "Pending" && status != "Done" && status != "Pending"){
                        updateTaskStatus(taskId, newStatus)
                        context?.let{
                            Toast.makeText(requireContext(), "Status güncellendi: $newStatus", Toast.LENGTH_SHORT).show()
                        }
                    }else if(currentTimeMillis > deadlineMillis && status != "Done"){
                        updateTaskStatus(taskId, "Overdue")
                        binding.statusRadioGroup.isEnabled = false
                        binding.radioDone.isEnabled = false
                        binding.radioInProgress.isEnabled = false
                        binding.taskListLayout.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.expired_red ))
                    }
                }
        }
    }

    private fun updateTaskStatus(taskId: String, status: String) {
        FirebaseFirestore.getInstance().collection("tasks")
            .document(taskId)
            .update("status", status)
            .addOnSuccessListener { document ->
                if (status == "Done") {
                    binding.statusRadioGroup.isEnabled = false
                    binding.radioDone.isEnabled = false
                    binding.radioInProgress.isEnabled = false
                    context?.let {
                        Toast.makeText(requireContext(), "Status güncellendi: $status", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun restorePreviousSelection() {
        when (previousStatus) {
            "Pending" -> binding.radioInProgress.isChecked = true
            "Done" -> binding.radioDone.isChecked = true
        }
    }


}