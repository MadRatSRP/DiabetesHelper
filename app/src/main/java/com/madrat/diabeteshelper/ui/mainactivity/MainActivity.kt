package com.madrat.diabeteshelper.ui.mainactivity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
        
        val navController = findNavController(R.id.fragment_container)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_diary_diabetes,
                R.id.navigation_diary_food
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)
    }
    
    fun showMessage(@StringRes messageRes: Int, isSuccess: Boolean = true) {
        with(binding) {
            with(statusMessage) {
                setText(messageRes)
                setBackgroundResource(
                    if (isSuccess)
                        R.color.success_message
                    else
                        R.color.unsuccess_message
                )
                visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    visibility = View.GONE
                }, 4000)
            }
        }
    }
}