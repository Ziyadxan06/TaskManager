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
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskName: String
    private lateinit var priority: String
    private lateinit var deadlineText: String
    private lateinit var assignedTo: String
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

    fun uploadData(){
        val taskName = binding.tasknameAdd.text.toString().trim()
        val priority = binding.taskPriority.text.toString().trim()
        val assignedTo = binding.taskAssignee.text.toString().trim()
        val deadlineDisplay = binding.taskDeadline.text.toString().trim()

        if (taskName.isEmpty() || priority.isEmpty() || assignedTo.isEmpty() || deadlineTimestamp == 0L) {
            Toast.makeText(requireContext(), "Bütün sahələri doldurun", Toast.LENGTH_SHORT).show()
        }else{

            val db = FirebaseFirestore.getInstance()
            val collection = db.collection("tasks")

            val newDocRef = collection.document()

            val taskData = hashMapOf(
                "id" to newDocRef.id,
                "name" to taskName,
                "priority" to priority,
                "assignedTo" to assignedTo,
                "deadline" to deadlineTimestamp,
                "status" to "Yeni"
            )

            newDocRef.set(taskData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Tapşırıq uğurla əlavə olundu", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Xəta baş verdi: ${e.message}", Toast.LENGTH_LONG).show()
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