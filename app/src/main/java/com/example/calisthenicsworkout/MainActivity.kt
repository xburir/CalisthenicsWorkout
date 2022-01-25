package com.example.calisthenicsworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.ActivityMainBinding
import com.example.calisthenicsworkout.databinding.NavDrawerHeaderBinding
import com.example.calisthenicsworkout.viewmodels.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)


        val application = requireNotNull(this).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = ProfileViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(ProfileViewModel::class.java)


        val drawerLayout = binding.drawerLayout
        val navView = binding.navView
        val header  = navView.getHeaderView(0)
        val headerBinding = NavDrawerHeaderBinding.bind(header)
        
        viewModel.currentUser.observe(this,{
            it?.let{
                headerBinding.userNameInHeader.text = it.userFullName
                headerBinding.imageView3.setImageURI(it.userImage)
                headerBinding.emailInHeader.text = it.userEmail
            }

        })




        val navController = this.findNavController(R.id.myNavHostFragment)


        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.homeFragment,R.id.myProfileFragment,R.id.allSkillsFragment,R.id.favSkillsFragment,
            R.id.myTrainingsFragment,R.id.allTrainingsFragment),drawerLayout)
        setupActionBarWithNavController(navController,appBarConfiguration)
        navView.setupWithNavController(navController)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.myNavHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    //    turn off keyboard
//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentFocus != null) {
//            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
//        }
//        return super.dispatchTouchEvent(ev)
//    }

}