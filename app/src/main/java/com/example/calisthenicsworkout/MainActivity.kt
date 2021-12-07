package com.example.calisthenicsworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.calisthenicsworkout.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);



        //find navigation controller in activity
        val navController = this.findNavController(R.id.myNavHostFragment)

        //if there is something in bundle because app waas shut down by OS, restore it
        /*
        if(savedInstanceState != null){
            variable = savedInstanceState.getInt("key");
        }*/

        //link navigation controller to action bar
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        Timber.i("onCreate");




    }
    //find navcontroller and then call navController.navigateUp
    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController,drawerLayout)
    }

    override fun onStart() {
        super.onStart()
        Timber.i("onStart");
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume");
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause");
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy");
    }

    override fun onRestart() {
        super.onRestart()
        Timber.i("onRestart");
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop");
    }

    //save something when OS destroys app for performance
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState);
        //              key    variable
        //outState.putInt("key",3);
    }
}