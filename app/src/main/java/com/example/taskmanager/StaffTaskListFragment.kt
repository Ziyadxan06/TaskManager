package com.example.taskmanager

import android.os.Bundle
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
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.databinding.FragmentSignUpBinding
import com.example.taskmanager.databinding.FragmentStaffTaskListBinding
import com.example.taskmanager.recyclerview.TasksAdapter
import com.example.taskmanager.recyclerview.TasksModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffTaskListFragment : Fragment() {

    private var _binding: FragmentStaffTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var taskList: ArrayList<TasksModel>
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStaffTaskListBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)


        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.optionsmenu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_settings -> {
                        findNavController().navigate(R.id.action_staffTaskListFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        taskList = ArrayList()
        recyclerView = binding.tasksRv
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        tasksAdapter = TasksAdapter(taskList) { selectedTask ->
            val action = StaffTaskListFragmentDirections.actionStaffTaskListFragmentToTaskDetailsDialogFragment(selectedTask.id)
            findNavController().navigate(action)
        }
        binding.tasksRv.adapter = tasksAdapter

        getData()
        taskUpdated()
    }

    private fun getData() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        FirebaseFirestore.getInstance().collection("tasks").addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_LONG).show()
            } else if (value != null && !value.isEmpty) {
                taskList.clear()
                val documents = value.documents

                for (document in documents) {

                    if(document.get("assignedTo") == currentUserEmail){
                        val id = document.get("id") as? String ?: ""
                        val assignedTo = document.get("assignedTo") as? String ?: ""
                        val taskTitle = document.get("name") as? String ?: ""
                        val priority = document.get("priority") as? String ?: ""
                        val status = document.get("status") as? String ?: ""
                        val deadline = document.get("deadline") as? Long ?: 0L

                        val task = TasksModel(id, taskTitle, deadline, assignedTo, priority, status)
                        taskList.add(task)
                    }
                }

                tasksAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun taskUpdated(){
        parentFragmentManager.setFragmentResultListener("taskUpdated", viewLifecycleOwner) { _, bundle ->
            val taskId = bundle.getString("updatedTaskId")
            val newStatus = bundle.getString("updatedStatus")

            if (!taskId.isNullOrBlank() && !newStatus.isNullOrBlank()) {
                val index = taskList.indexOfFirst { it.id == taskId }
                if (index != -1) {
                    taskList[index] = taskList[index].copy(status = newStatus)
                    tasksAdapter.notifyItemChanged(index)
                }
            }
        }
    }
}