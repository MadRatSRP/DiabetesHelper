package com.madrat.diabeteshelper

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.madrat.diabeteshelper.databinding.ActivityMainBinding

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

        val drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, R.string.open, R.string.close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        NavigationUI.setupWithNavController(binding.navigationView, navController)
        NavigationUI.setupActionBarWithNavController(this,
            navController, binding.drawerLayout)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}