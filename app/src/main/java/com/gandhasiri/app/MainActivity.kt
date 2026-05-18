package com.gandhasiri.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gandhasiri.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        
        navHostFragment?.let {
            val navController = it.navController
            
            val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_register, R.id.navigation_tracker, R.id.navigation_security
            ).build()
            
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHostFragment?.navController?.navigateUp() ?: super.onSupportNavigateUp()
    }
}
