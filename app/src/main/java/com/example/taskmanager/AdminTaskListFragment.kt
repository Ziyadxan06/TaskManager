package com.example.taskmanager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.recyclerview.InventoryModel
import com.example.taskmanager.recyclerview.TasksAdapter
import com.example.taskmanager.recyclerview.TasksModel
import com.example.taskmanager.recyclerview.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class AdminTaskListFragment : Fragment() {

    private var _binding: AdminFragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var taskList: ArrayList<TasksModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = AdminFragmentTaskListBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar2)

        binding.progressBar.visibility = View.VISIBLE

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "staff"
                if(role == "admin" || role == "superadmin"){
                    delete()
                }
                setupUiByRole(role)
                setUpFilterListenerTasks(role, currentUserEmail)
            }

        taskList = ArrayList()



        recyclerView = binding.taskListRV
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        tasksAdapter = TasksAdapter(taskList) { selectedTask ->
            val action = AdminTaskListFragmentDirections.actionTaskListFragmentToTaskDetailsDialogFragment(selectedTask.id)
            findNavController().navigate(action)
        }
        binding.taskListRV.adapter = tasksAdapter

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.optionsmenu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_settings -> {
                        findNavController().navigate(R.id.action_adminTaskListFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        val workRequest = PeriodicWorkRequestBuilder<DeadlineCheckWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "DeadlineCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun setupUiByRole(role: String) {
        if (role == "admin" || role == "superadmin") {
            binding.floatingActionButton.visibility = View.VISIBLE
        } else {
            binding.floatingActionButton.visibility = View.GONE
        }
    }

    private fun setUpFilterListenerTasks(role: String, email: String){
        val options = if(role == "admin" || role == "superadmin"){
            arrayOf("All Tasks", "My Tasks", "By User", "By Deadline")
        }else{
            arrayOf("All Tasks", "By Deadline")
        }

        binding.progressBar.visibility = View.GONE

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        binding.filterSpinnerTask.adapter = adapter

        binding.filterSpinnerTask.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(role == "admin" || role == "superadmin"){
                    when(position){
                        0 -> fetchAllTasks(role, email)
                        1 -> fetchUserTasks(email)
                        2 -> showEmailInputDialog()
                        3 -> showArrivalDatePicker(role, email)
                    }
                }else{
                    when(position){
                        0 -> fetchAllTasks(role, email)
                        1 -> showArrivalDatePicker(role, email)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    private fun fetchAllTasks(role: String, email: String){
        taskList.clear()

        val query = if(role == "admin" || role == "superadmin"){
            FirebaseFirestore.getInstance().collection("tasks")
        }else{
            FirebaseFirestore.getInstance().collection("tasks").whereEqualTo("assignedTo", email)
        }
        query.whereIn("status", listOf("Pending", "Yeni")).get().addOnSuccessListener { documents ->
            for(document in documents){
                val id = document.get("id") as? String ?: ""
                val assignedTo = document.get("assignedTo") as? String ?: ""
                val taskTitle = document.get("name") as? String ?: ""
                val priority = document.get("priority") as? String ?: ""
                val status = document.get("status") as? String ?: ""
                val userName = document.get("userName") as? String ?: ""
                val deadline = document.get("deadline") as? Long ?: 0L

                val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
                taskList.add(task)
                tasksAdapter.notifyDataSetChanged()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchUserTasks(email: String){
        taskList.clear()

        FirebaseFirestore.getInstance().collection("tasks")
            .whereEqualTo("assignedTo", email)
            .whereIn("status", listOf("Pending", "Yeni"))
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    val id = document.get("id") as? String ?: ""
                    val assignedTo = document.get("assignedTo") as? String ?: ""
                    val taskTitle = document.get("name") as? String ?: ""
                    val priority = document.get("priority") as? String ?: ""
                    val status = document.get("status") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val deadline = document.get("deadline") as? Long ?: 0L

                    val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
                    taskList.add(task)
                    tasksAdapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchInventoryByEmail(email: String) {
        FirebaseFirestore.getInstance().collection("tasks")
            .whereEqualTo("assignedTo", email)
            .whereIn("status", listOf("Pending", "Yeni"))
            .get()
            .addOnSuccessListener {documents ->
                for (document in documents) {
                    val id = document.get("id") as? String ?: ""
                    val assignedTo = document.get("assignedTo") as? String ?: ""
                    val taskTitle = document.get("name") as? String ?: ""
                    val priority = document.get("priority") as? String ?: ""
                    val status = document.get("status") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val deadline = document.get("deadline") as? Long ?: 0L

                    val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
                    taskList.add(task)
                    tasksAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showEmailInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_user_filter, null)
        val autoCompleteTextView = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteUser)

        autoCompleteTextView.inputType = InputType.TYPE_CLASS_TEXT
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.requestFocus()

        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userList = mutableListOf<UserModel>()
                val displayList = mutableListOf<String>()

                for (doc in documents) {
                    val username = doc.getString("username") ?: ""
                    val email = doc.getString("useremail") ?: ""
                    if (email.isNotEmpty() && username.isNotEmpty()) {
                        val user = UserModel(username = username, useremail = email)
                        userList.add(user)
                        displayList.add("${user.username} (${user.useremail})")
                    }
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, displayList)
                autoCompleteTextView.setAdapter(adapter)


                autoCompleteTextView.postDelayed({
                    autoCompleteTextView.showDropDown()
                }, 200)

                AlertDialog.Builder(requireContext())
                    .setTitle("Filter by User")
                    .setView(dialogView)
                    .setPositiveButton("OK") { dialog, _ ->
                        val selectedText = autoCompleteTextView.text.toString().trim()
                        val selectedUser = userList.find {
                            "${it.username} (${it.useremail})" == selectedText
                        }

                        if (selectedUser != null) {
                            taskList.clear()
                            tasksAdapter.notifyDataSetChanged()
                            fetchInventoryByEmail(selectedUser.useremail ?: "")
                        } else {
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                        }

                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching users: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }



    private fun showArrivalDatePicker(role: String, userId: String) {
        val calendar = java.util.Calendar.getInstance()

        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val startOfDay = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = java.util.Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 23, 59, 59)
                set(java.util.Calendar.MILLISECOND, 999)
            }.timeInMillis

            taskList.clear()
            tasksAdapter.notifyDataSetChanged()
            fetchInventoryByDateRange(startOfDay, endOfDay, role, userId)

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

        datePicker.setTitle("Select Deadline")
        datePicker.show()
    }

     fun fetchInventoryByDateRange(start: Long, end: Long, role: String, email: String) {
        val query = if(role == "admin" || role == "superadmin"){
            FirebaseFirestore.getInstance().collection("tasks")
        }else{
            FirebaseFirestore.getInstance().collection("tasks").whereEqualTo("assignedTo", email)
        }
        query.whereGreaterThanOrEqualTo("deadline", start)
            .whereLessThanOrEqualTo("deadline", end)
            .whereIn("status", listOf("Pending", "Yeni"))
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.get("id") as? String ?: ""
                    val assignedTo = document.get("assignedTo") as? String ?: ""
                    val taskTitle = document.get("name") as? String ?: ""
                    val priority = document.get("priority") as? String ?: ""
                    val status = document.get("status") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val deadline = document.get("deadline") as? Long ?: 0L

                    val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
                    taskList.add(task)
                    tasksAdapter.notifyDataSetChanged()
                }
            }
        }

    private fun delete(){
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedTask = taskList[position]

                FirebaseFirestore.getInstance()
                    .collection("tasks")
                    .document(deletedTask.id)
                    .delete()
                    .addOnSuccessListener {
                        taskList.removeAt(position)
                        tasksAdapter.notifyItemRemoved(position)
                        Toast.makeText(requireContext(), "Task Deleted", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Task could not deleted",Toast.LENGTH_LONG).show()
                    }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.taskListRV)
    }
}