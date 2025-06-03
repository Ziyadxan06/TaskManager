package com.example.taskmanager


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.util.concurrent.TimeUnit

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!
    var deadlineTimestamp: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE

        binding.toolbarAddTask.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarAddTask.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.taskDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                    val selectedDateTime = Calendar.getInstance()
                    selectedDateTime.set(year, month, dayOfMonth, hour, minute)

                    deadlineTimestamp = selectedDateTime.timeInMillis

                    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                    binding.taskDeadline.setText(formatter.format(selectedDateTime.time))

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                timePicker.show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePicker.show()
        }

        binding.btnAdd.setOnClickListener {
            uploadData()
        }

        setupAssigneeEmailDropdown()
    }

    fun uploadData() {
        val taskName = binding.tasknameAdd.text.toString().trim()
        val priority = binding.taskPriority.text.toString().trim()
        val assignedTo = binding.taskAssignee.text.toString().trim()
        val userName = binding.usernameAdd.text.toString().trim()

        if (taskName.isEmpty() || priority.isEmpty() || assignedTo.isEmpty() || deadlineTimestamp == 0L) {
            Toast.makeText(requireContext(), "Bütün sahələri doldurun", Toast.LENGTH_SHORT).show()
        } else {
            val db = FirebaseFirestore.getInstance()
            val collection = db.collection("tasks")

            val newDocRef = collection.document()

            val taskData = hashMapOf(
                "id" to newDocRef.id,
                "name" to taskName,
                "priority" to priority,
                "assignedTo" to assignedTo,
                "userName" to userName,
                "deadline" to deadlineTimestamp,
                "status" to "Yeni",
                "shown" to false
            )

            newDocRef.set(taskData)
                .addOnSuccessListener {
                    context?.let {
                        Toast.makeText(requireContext(), "Tapşırıq uğurla əlavə olundu", Toast.LENGTH_SHORT).show()
                    }
                    findNavController().navigate(R.id.action_addTaskFragment_to_adminTaskListFragment)
                    findNavController().popBackStack(R.id.addTaskFragment, true)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Xəta baş verdi: ${e.message}", Toast.LENGTH_LONG).show()
                }


            val delay = deadlineTimestamp - System.currentTimeMillis()
            if (delay > 0) {
                val data = workDataOf("taskName" to taskName)

                val deadlineWork = OneTimeWorkRequestBuilder<DeadlinePassedWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(requireContext()).enqueue(deadlineWork)
            }
        }
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

                binding.taskAssignee.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Email siyahısı yüklənmədi: ${it.message}", Toast.LENGTH_LONG).show()
            }

        binding.taskAssignee.setOnItemClickListener { _, _, position, _ ->
            val selectedEmail = binding.taskAssignee.adapter.getItem(position) as String

            FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("useremail", selectedEmail)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    val name = result.documents.firstOrNull()?.getString("username") ?: "-"
                    binding.usernameAdd.setText(name)
                }
        }
    }
}