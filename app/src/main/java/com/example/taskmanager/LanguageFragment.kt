package com.example.taskmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.FragmentArchiveBinding
import com.example.taskmanager.databinding.FragmentLanguageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE
        binding.toolbarLanguage.setNavigationIcon(R.drawable.ic_back)
        binding.toolbarLanguage.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbarLanguage.visibility = View.VISIBLE
    }
}