    package com.example.taskmanager

    import android.app.AlertDialog
    import android.app.DatePickerDialog
    import android.graphics.Color
    import android.os.Bundle
    import android.text.InputType
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
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.core.view.MenuProvider
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.ItemTouchHelper
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.taskmanager.databinding.FragmentBackgroundTaskBinding
    import com.example.taskmanager.databinding.FragmentInventoryListBinding
    import com.example.taskmanager.recyclerview.TasksAdapter
    import com.example.taskmanager.recyclerview.TasksModel
    import com.example.taskmanager.recyclerview.UserModel
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore

    class BackgroundTaskFragment : Fragment() {

        private var _binding: FragmentBackgroundTaskBinding? = null
        private val binding get() = _binding!!

        private lateinit var recyclerView: RecyclerView
        private lateinit var backgroundtasksAdapter: TasksAdapter
        private lateinit var taskList: ArrayList<TasksModel>
        private var now = System.currentTimeMillis()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            _binding = FragmentBackgroundTaskBinding.inflate(inflater, container, false)
            return (binding.root)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarBackground)

            taskList = ArrayList()

            recyclerView = binding.backgroundRecyclerview
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            backgroundtasksAdapter = TasksAdapter(taskList, onItemClick =  { selectedTask ->
                val action = BackgroundTaskFragmentDirections.actionBackgroundTaskFragmentToTaskDetailsDialogFragment(selectedTask.id)
                findNavController().navigate(action)
            },onOverdueDetected = { overdueTask ->
                FirebaseFirestore.getInstance().collection("tasks").document(overdueTask.id)
                    .update("status", "Overdue")
            })
            binding.backgroundRecyclerview.adapter = backgroundtasksAdapter

            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            delete()

            FirebaseFirestore.getInstance().collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role") ?: "staff"
                    setUpFilterListenerTasks(role, currentUserEmail)
                }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menu.clear()
                    menuInflater.inflate(R.menu.optionsmenu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.menu_settings -> {
                            findNavController().navigate(R.id.action_backgroundTaskFragment_to_settingsFragment)
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner)
        }

        private fun setUpFilterListenerTasks(role: String, email: String){
            val options = if(role == "admin" || role == "superadmin"){
                arrayOf("${context?.getString(R.string.alltasks)}", "${context?.getString(R.string.mytasks)}", "${context?.getString(R.string.byuser_tasks)}", "${context?.getString(R.string.bydeadline)}")
            }else{
                arrayOf("${context?.getString(R.string.alltasks)}", "${context?.getString(R.string.bydeadline)}")
            }

            binding.backgroundprogressBar.visibility = View.GONE

            val adapter = object : ArrayAdapter<String>(
                requireContext(),
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                options
            ) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    (view as TextView).setTextColor(Color.WHITE)
                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.spinner_dropdown_bg))
                    return view
                }

                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).setTextColor(Color.WHITE)
                    return view
                }
            }

            binding.backgroundfilterSpinnerTask.adapter = adapter

            binding.backgroundfilterSpinnerTask.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
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

        private fun fetchAllTasks(role: String, email: String) {
            taskList.clear()
            backgroundtasksAdapter.notifyDataSetChanged()

            val commonList = mutableSetOf<String>()

            val queryBase = if (role == "admin" || role == "superadmin") {
                FirebaseFirestore.getInstance().collection("tasks")
            } else {
                FirebaseFirestore.getInstance().collection("tasks").whereEqualTo("assignedTo", email)
            }

            queryBase.whereLessThan("deadline", now)
                .get()
                .addOnSuccessListener { deadlineDocs ->
                    for (doc in deadlineDocs) {
                        val id = doc.getString("id") ?: continue
                        if (commonList.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }

            queryBase.whereEqualTo("status", "Done")
                .get()
                .addOnSuccessListener { doneDocs ->
                    for (doc in doneDocs) {
                        val id = doc.getString("id") ?: continue
                        if (commonList.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }
        }


        private fun fetchUserTasks(email: String) {
            taskList.clear()
            backgroundtasksAdapter.notifyDataSetChanged()

            val seenTaskIds = mutableSetOf<String>()

            val tasksCollection = FirebaseFirestore.getInstance().collection("tasks")
                .whereEqualTo("assignedTo", email)

            tasksCollection.whereLessThan("deadline", now)
                .get()
                .addOnSuccessListener { deadlineDocs ->
                    for (doc in deadlineDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }

            tasksCollection.whereEqualTo("status", "Done")
                .get()
                .addOnSuccessListener { doneDocs ->
                    for (doc in doneDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }
        }


        private fun fetchInventoryByEmail(email: String) {
            taskList.clear()
            backgroundtasksAdapter.notifyDataSetChanged()

            val seenTaskIds = mutableSetOf<String>()

            val tasksCollection = FirebaseFirestore.getInstance()
                .collection("tasks")
                .whereEqualTo("assignedTo", email)

            tasksCollection.whereLessThan("deadline", now)
                .get()
                .addOnSuccessListener { deadlineDocs ->
                    for (doc in deadlineDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }

            tasksCollection.whereEqualTo("status", "Done")
                .get()
                .addOnSuccessListener { doneDocs ->
                    for (doc in doneDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
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
                        .setTitle("${context?.getString(R.string.alert_title_filter)}")
                        .setView(dialogView)
                        .setPositiveButton("${context?.getString(R.string.positive_button_filter)}") { dialog, _ ->
                            val selectedText = autoCompleteTextView.text.toString().trim()
                            val selectedUser = userList.find {
                                "${it.username} (${it.useremail})" == selectedText
                            }

                            if (selectedUser != null) {
                                taskList.clear()
                                backgroundtasksAdapter.notifyDataSetChanged()
                                fetchInventoryByEmail(selectedUser.useremail ?: "")
                            } else {
                                Toast.makeText(requireContext(), "${context?.getString(R.string.error_notfound)}", Toast.LENGTH_SHORT).show()
                            }

                            dialog.dismiss()
                        }
                        .setNegativeButton("${context?.getString(R.string.negative_button_filter)}") { dialog, _ -> dialog.cancel() }
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "${context?.getString(R.string.error)}: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
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
                backgroundtasksAdapter.notifyDataSetChanged()
                fetchInventoryByDateRange(startOfDay, endOfDay, role, userId)

            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))

            datePicker.setTitle("Select Deadline")
            datePicker.show()
        }

        fun fetchInventoryByDateRange(start: Long, end: Long, role: String, email: String) {
            taskList.clear()
            backgroundtasksAdapter.notifyDataSetChanged()

            val seenTaskIds = mutableSetOf<String>()

            val queryBase = if (role == "admin" || role == "superadmin") {
                FirebaseFirestore.getInstance().collection("tasks")
            } else {
                FirebaseFirestore.getInstance().collection("tasks").whereEqualTo("assignedTo", email)
            }

            queryBase
                .whereGreaterThanOrEqualTo("deadline", start)
                .whereLessThanOrEqualTo("deadline", end)
                .get()
                .addOnSuccessListener { dateDocs ->
                    for (doc in dateDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
                }

            queryBase
                .whereEqualTo("status", "Done")
                .get()
                .addOnSuccessListener { doneDocs ->
                    for (doc in doneDocs) {
                        val id = doc.getString("id") ?: continue
                        if (seenTaskIds.add(id)) {
                            taskList.add(doc.toTaskModel())
                        }
                    }
                    backgroundtasksAdapter.notifyDataSetChanged()
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
                            backgroundtasksAdapter.notifyItemRemoved(position)
                            Toast.makeText(requireContext(), "Task ${context?.getString(R.string.successful_deletion)}", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                            Toast.makeText(requireContext(), "Task ${context?.getString(R.string.unsuccessful_deletion)}",Toast.LENGTH_LONG).show()
                        }
                }
            }

            ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.backgroundRecyclerview)
        }

        private fun com.google.firebase.firestore.DocumentSnapshot.toTaskModel(): TasksModel {
            val id = get("id") as? String ?: ""
            val assignedTo = get("assignedTo") as? String ?: ""
            val taskTitle = get("name") as? String ?: ""
            val priority = get("priority") as? String ?: ""
            val status = get("status") as? String ?: ""
            val userName = get("userName") as? String ?: ""
            val deadline = get("deadline") as? Long ?: 0L

            return TasksModel(id, taskTitle, deadline, assignedTo, priority, status, userName)
        }


    }