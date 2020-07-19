package com.madrat.diabeteshelper.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.madrat.diabeteshelper.R
import com.madrat.diabeteshelper.databinding.ActivityMainBinding
import com.madrat.diabeteshelper.logic.util.viewBinding

class MainActivity : AppCompatActivity() {
    // ViewBinding
    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = Navigation.findNavController(
            this, R.id.nav_host_fragment
        )

        NavigationUI.setupActionBarWithNavController(this,
            navController, binding.drawerLayout)
    }
}