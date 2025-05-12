package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.taskmanager.databinding.FragmentSignInBinding
import com.example.taskmanager.databinding.FragmentSignUpBinding
import com.example.taskmanager.databinding.FragmentStaffTaskListBinding

class StaffTaskListFragment : Fragment() {

    private var _binding: FragmentStaffTaskListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStaffTaskListBinding.inflate(inflater, container, false)
        return (binding.root)
    }
}