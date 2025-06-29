package com.example.taskmanager

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.taskmanager.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNavigationView = binding.bottomNavigation

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.signInFragment, R.id.signUpFragment -> bottomNavigationView.visibility = View.GONE
                R.id.userManagement, R.id.userDetailsDialogFragment -> bottomNavigationView.visibility = View.GONE
                R.id.editInventoryItemFragment -> bottomNavigationView.visibility = View.GONE
                else -> bottomNavigationView.visibility = View.VISIBLE
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.adminTaskListFragment,
                R.id.backgroundTaskFragment,
                R.id.inventoryListFragment,
                R.id.archiveFragment,
                R.id.inventoryLogFragment
            )
        )

        bottomNavigationView.setupWithNavController(navController)
    }

    override fun attachBaseContext(newBase: Context) {
        val langCode = PreferenceHelper.getLanguage(newBase)
        val context = LocalHelper.setLocale(newBase, langCode)
        super.attachBaseContext(context)
    }
}