package com.example.taskmanager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.AdminFragmentTaskListBinding
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.recyclerview.TasksAdapter
import com.example.taskmanager.recyclerview.TasksModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminTaskListFragment : Fragment() {

    private var _binding: AdminFragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var taskList: ArrayList<TasksModel>
    private lateinit var db: FirebaseFirestore

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

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users")
            .document(currentUserUid)
            .get()
            .addOnSuccessListener { document ->
                val role = document.getString("role") ?: "staff"
                setupUiByRole(role)
                fetchTasks(role)
            }


        db = FirebaseFirestore.getInstance()

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
    }

    private fun setupUiByRole(role: String) {
        if (role == "admin" || role == "superadmin") {
            binding.floatingActionButton.visibility = View.VISIBLE
        } else {
            binding.floatingActionButton.visibility = View.GONE
        }
    }

    private fun fetchTasks(role: String) {
        val collection = FirebaseFirestore.getInstance().collection("tasks")

        val query = if (role == "admin" || role == "superadmin") {
            collection
        } else {
            val email = FirebaseAuth.getInstance().currentUser?.email
            collection.whereEqualTo("assignedTo", email)
        }

        taskList.clear()
        tasksAdapter.notifyDataSetChanged()

        query.addSnapshotListener { snapshot, error ->

            if (error != null) {
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            binding.progressBar.visibility = View.GONE

            if (snapshot != null && !snapshot.isEmpty) {
                taskList.clear()
                for (document in snapshot.documents) {
                    val id = document.get("id") as? String ?: ""
                    val assignedTo = document.get("assignedTo") as? String ?: ""
                    val taskTitle = document.get("name") as? String ?: ""
                    val priority = document.get("priority") as? String ?: ""
                    val status = document.get("status") as? String ?: ""
                    val userName = document.get("userName") as? String ?: ""
                    val deadline = document.get("deadline") as? Long ?: 0L

                    val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
                    taskList.add(task)
                }

                tasksAdapter.notifyDataSetChanged()
            } else {
                taskList.clear()
                tasksAdapter.notifyDataSetChanged()
            }
        }
    }
}