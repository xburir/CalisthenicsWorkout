package com.example.calisthenicsworkout

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.ActivityMainBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        val application = requireNotNull(this).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(SkillViewModel::class.java)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //find navigation controller in activity
        val navController = this.findNavController(R.id.myNavHostFragment)
        //link navigation controller to action bar
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        Timber.i("onCreate");


        readFireStoreData()


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

    private fun readFireStoreData(){
        val db = FirebaseFirestore.getInstance()
        val fbStorage = FirebaseStorage.getInstance()
        val backUpPicRef = fbStorage.reference.child("skillImages").child("nothing.png")
        db.collection("skills").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val desc = entry.data.getValue("description").toString()
                    val type = entry.data.getValue("type").toString()
                    val pictureRef = fbStorage.reference.child("skillImages").child("$id.png")
                    var bitmap: Bitmap
                    pictureRef.downloadUrl
                        .addOnFailureListener {
                            backUpPicRef.downloadUrl.addOnSuccessListener {
                                viewModel.viewModelScope.launch{
                                    bitmap = getBitmap(it)
                                    val skill = Skill(id,name,desc,bitmap,type)
                                    viewModel.addSkillToDatabase(skill)
                                }
                            }
                        }
                        .addOnSuccessListener {
                            viewModel.viewModelScope.launch{
                                bitmap = getBitmap(it)
                                val skill = Skill(id,name,desc,bitmap,type)
                                viewModel.addSkillToDatabase(skill)
                            }
                        }
                }
            }
        }
        db.collection("skillAndSkillsCrossRef").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val crossRef = SkillAndSkillCrossRef(
                        entry.data.getValue("skillId").toString(),
                        entry.data.getValue("childId").toString(),
                        entry.data.getValue("amount").toString().toInt()
                    )
                    viewModel.addSkillAndSkillCrossRef(crossRef)
                }
            }
        }
        db.collection("userAndSkillCrossRef").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                for (entry in it.result!!){
                    viewModel.userAndSkillCrossRef(entry.data.getValue("userId").toString(),entry.data.getValue("skillId").toString(),entry.data.getValue("liked").toString())
                }
            }

        }
        db.collection("trainings").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    pictureRef.downloadUrl
                        .addOnFailureListener {
                            backUpPicRef.downloadUrl.addOnSuccessListener {
                                viewModel.viewModelScope.launch {
                                    val bitmap = getBitmap(it)
                                    val training = Training(name,target,id,bitmap)
                                    viewModel.addTrainingToDatabase(training)
                                }
                            }
                        }
                        .addOnSuccessListener {
                        viewModel.viewModelScope.launch {
                            val bitmap = getBitmap(it)
                            val training = Training(name,target,id,bitmap)
                            viewModel.addTrainingToDatabase(training)
                        }
                    }
                }
            }
        }



    }

    private suspend fun getBitmap(source: Uri): Bitmap {
        val loading = ImageLoader(this)
        val request = ImageRequest.Builder(this).data(source).build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }

}