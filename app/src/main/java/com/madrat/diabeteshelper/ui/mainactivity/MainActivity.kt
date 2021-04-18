package com.madrat.diabeteshelper.ui.mainactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ViewBinding initialization
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        val navController = Navigation.findNavController(
            this, R.id.nav_host_fragment
        )

        NavigationUI.setupActionBarWithNavController(this,
            navController, binding.drawerLayout)
    }
}