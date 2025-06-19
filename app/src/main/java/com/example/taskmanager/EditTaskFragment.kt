package com.example.taskmanager

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentEditTaskBinding
import com.example.taskmanager.databinding.FragmentTaskDetailsDialogBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

class EditTaskFragment : Fragment() {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val args: EditTaskFragmentArgs by navArgs()
    private lateinit var updatedName: String
    private lateinit var updatedDeadline: String
    private lateinit var updatedAssignee: String
    private lateinit var updatedPriority: String
    private lateinit var updatedUsername: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var taskId = args.taskId

        FirebaseFirestore.getInstance().collection("tasks")
            .document(taskId)
            .get()
            .addOnSuccessListener{ document ->
                binding.taskNameEdit.setText(document.getString("name") ?: "-")
                binding.taskPriorityEdit.setText(document.getString("priority") ?: "-")
                binding.taskAssigneeEdit.setText(document.getString("assignedTo") ?: "-")
                binding.usernameEdit.setText(document.getString("userName") ?: "-")

                val deadlineMillis = document.getLong("deadline") ?: 0L
                val formatted = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(
                    Date(deadlineMillis)
                )
                binding.taskDeadlineEdit.setText(formatted)
            }.addOnFailureListener {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
            }

        binding.toolbarEditTask.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarEditTask.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSaveTask.setOnClickListener {
            updatedName = binding.taskNameEdit.text.toString().trim()
            updatedDeadline = binding.taskDeadlineEdit.text.toString().trim()
            updatedAssignee = binding.taskAssigneeEdit.text.toString().trim()
            updatedPriority = binding.taskPriorityEdit.text.toString().trim()
            updatedUsername = binding.usernameEdit.text.toString().trim()


            if(updatedName.isEmpty() || updatedDeadline.isEmpty() || updatedAssignee.isEmpty() || updatedPriority.isEmpty()){
                Toast.makeText(context, "Butun xanalari doldurun", Toast.LENGTH_LONG).show()
            }else{
                val updateMap = hashMapOf<String, Any>(
                    "name" to updatedName,
                    "priority" to updatedPriority,
                    "assignedTo" to updatedAssignee,
                    "userName" to updatedUsername
                )

                FirebaseFirestore.getInstance().collection("tasks")
                    .document(args.taskId)
                    .update(updateMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Tapşırıq yeniləndi", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Xəta baş verdi: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        setupAssigneeEmailDropdown()
    }

    private fun setupAssigneeEmailDropdown() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { result ->
                val emailList = result.mapNotNull { it.getString("useremail") }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    emailList
                )

                binding.taskAssigneeEdit.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Email siyahısı yüklənmədi: ${it.message}", Toast.LENGTH_LONG).show()
            }

        binding.taskAssigneeEdit.setOnItemClickListener { _, _, position, _ ->
            val selectedEmail = binding.taskAssigneeEdit.adapter.getItem(position) as String

            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("useremail", selectedEmail)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    val name = result.documents.firstOrNull()?.getString("username") ?: "-"
                    binding.usernameEdit.setText(name)
                }
        }

    }
}