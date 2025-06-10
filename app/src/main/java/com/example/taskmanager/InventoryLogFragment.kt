package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.FragmentAddTaskBinding
import com.example.taskmanager.databinding.FragmentInventoryLogBinding
import com.example.taskmanager.recyclerview.LogAdapter
import com.example.taskmanager.recyclerview.LogModel

class InventoryLogFragment : Fragment() {

    private var _binding: FragmentInventoryLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var logAdapter: LogAdapter
    private lateinit var logList: ArrayList<LogModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInventoryLogBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logList = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        logAdapter = LogAdapter(logList)
        binding.logRecyclerView.adapter = logAdapter
    }
}