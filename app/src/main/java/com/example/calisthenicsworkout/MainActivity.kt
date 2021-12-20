package com.example.calisthenicsworkout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.ActivityMainBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
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



    private fun readFireStoreData(){
        val db = FirebaseFirestore.getInstance()
        val fbStorage = FirebaseStorage.getInstance()
        val fbAuth = FirebaseAuth.getInstance()
        db.collection("skills").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val desc = entry.data.getValue("description").toString()
                    val type = entry.data.getValue("type").toString()
                    val pictureRef = fbStorage.reference.child("skillImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.nothing)
                    val skill = Skill(id,name,desc,bitmap,type)
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModel.viewModelScope.launch{
                                skill.skillImage = getBitmap(it)
                                viewModel.addSkillToDatabase(skill)
                            }
                        }
                        .addOnFailureListener {
                            viewModel.viewModelScope.launch {
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
        db.collection("trainings").whereEqualTo("owner","admin").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.nothing)
                    val training = Training(name,target,id,"admin",bitmap,numberOfExercises)
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                        viewModel.viewModelScope.launch {
                            training.image = getBitmap(it)
                            viewModel.addTrainingToDatabase(training)
                        }
                    }
                        .addOnFailureListener {
                            viewModel.viewModelScope.launch {
                                viewModel.addTrainingToDatabase(training)
                            }
                        }

                }
            }
        }
        db.collection("trainings").whereEqualTo("owner",fbAuth.currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.nothing)
                    val training = Training(name,target,id,fbAuth.currentUser!!.uid,bitmap,numberOfExercises)
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModel.viewModelScope.launch {
                                training.image = getBitmap(it)
                                viewModel.addTrainingToDatabase(training)
                            }
                        }
                        .addOnFailureListener {
                            viewModel.viewModelScope.launch {
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

    fun saveFireStore(crossRef: SkillAndSkillCrossRef){
        val db = FirebaseFirestore.getInstance()
        val mappedThing: MutableMap<String,Any> = HashMap()
        mappedThing["skillId"] = crossRef.skillId
        mappedThing["childId"] = crossRef.childSkillId
        mappedThing["amount"] = crossRef.minAmount



        db.collection("skillAndSkillsCrossRef").add(mappedThing)
            .addOnSuccessListener {
                Log.i("Debug","added succesfully")
            }
            .addOnFailureListener{
                Log.i("Debug","not added")
            }
    }

//    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        if (currentFocus != null) {
//            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
//        }
//        return super.dispatchTouchEvent(ev)
//    }

}