package com.example.calisthenicsworkout.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.VideoActivity
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.example.calisthenicsworkout.fragments.skill.SkillFragmentDirections
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(SkillViewModel::class.java)

        setHasOptionsMenu(true)


        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_home,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.toString() == "Read Online Data"){
            readFireStoreData()
            Toast.makeText(context,"Fetching Data from online database",Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    fun readFireStoreData(){
        val db = FirebaseFirestore.getInstance()
        val fbStorage = FirebaseStorage.getInstance()
        val fbAuth = FirebaseAuth.getInstance()

        getSkillsFromFireBase(db,fbStorage)
        getSkillsAndSkillCrossRefFromFireBase(db)
        getUserAndSkillCrossRefFromFireBase(db)
        getPredefinedTrainings(db,fbStorage)
        getUsersTrainings(db,fbAuth,fbStorage)
        getExercisesForTrainings(db)
        getUser(fbAuth)
        checkIfNewSkillsWereAdded(fbAuth,db)
    }

    private fun getExercisesForTrainings(db: FirebaseFirestore) {
        viewModel.allTrainings.observe(viewLifecycleOwner,{ LiveDataList ->
            LiveDataList?.let { trainingList ->
                trainingList.forEach { training ->
                    db.collection("exercises").whereEqualTo("trainingId",training.id).get().addOnCompleteListener{
                        if(it.isSuccessful){
                            for(entry in it.result!!){
                                val skillId = entry.data.getValue("skillId").toString()
                                val order = entry.data.getValue("order").toString().toInt()
                                val trainingId = entry.data.getValue("trainingId").toString()
                                val reps = entry.data.getValue("reps").toString()
                                val sets = entry.data.getValue("sets").toString()
                                viewModel.allSkills.observe(viewLifecycleOwner,{
                                    it?.let { skills ->
                                        skills.forEach { skill ->
                                            if(skill.skillId == skillId){
                                                viewModel.viewModelScope.launch {
                                                    val exercise = Exercise(trainingId,skillId,sets,reps,skill.skillImage,skill.skillName,order)
                                                    viewModel.insertExercise(exercise)
                                                }
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        })
    }

    private fun getUser(fbAuth: FirebaseAuth) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(fbAuth.currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                val email =  it.result!!.data?.getValue("userEmail").toString()
                val name = it.result!!.data?.getValue("userFullName").toString()
                val id = it.result!!.id
                viewModel.viewModelScope.launch {
                    viewModel.insertUser(User(id,email,name))
                }
            }
        }
    }

    private fun checkIfNewSkillsWereAdded(fbAuth: FirebaseAuth,db: FirebaseFirestore) {
        viewModel.allSkills.observe(this,{
            it?.let{ skills ->
                skills.forEach { skill ->
                    var crossRefFound = false
                    viewModel.database.getUserSkillCrossRefs(fbAuth.currentUser!!.uid).observe(this,{ liveDataList ->
                        liveDataList?.let { listOfUserSkillCrossRefs ->
                            listOfUserSkillCrossRefs.forEach { crossRef ->
                                if(crossRef.skillId == skill.skillId){
                                    crossRefFound = true
                                }
                            }
                            if(!crossRefFound){
                                Log.i("Debug","Crossref between "+fbAuth.currentUser!!.uid.toString()+" and " + skill.skillName+"  not found, adding")
                                viewModel.viewModelScope.launch {
                                    viewModel.userAndSkillCrossRef(fbAuth.currentUser!!.uid,skill.skillId,"false")
                                }
                                val mappedThing: MutableMap<String,Any> = HashMap()
                                mappedThing["skillId"] = skill.skillId
                                mappedThing["userId"] = fbAuth.currentUser!!.uid
                                mappedThing["liked"] = false
                                db.collection("userAndSkillCrossRef").add(mappedThing)
                            }

                        }
                    })
                }

            }
        })
    }

    private fun getUsersTrainings(db: FirebaseFirestore, fbAuth: FirebaseAuth, fbStorage: FirebaseStorage) {
        db.collection("trainings").whereEqualTo("owner",fbAuth.currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.nothing)
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

    private fun getPredefinedTrainings(db: FirebaseFirestore, fbStorage: FirebaseStorage) {
        db.collection("trainings").whereEqualTo("owner","admin").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.nothing)
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
    }

    private fun getUserAndSkillCrossRefFromFireBase(db: FirebaseFirestore) {
        db.collection("userAndSkillCrossRef").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                for (entry in it.result!!){
                    viewModel.viewModelScope.launch {
                        viewModel.userAndSkillCrossRef(entry.data.getValue("userId").toString(),entry.data.getValue("skillId").toString(),entry.data.getValue("liked").toString())
                    }

                }
            }
        }
    }

    private fun getSkillsAndSkillCrossRefFromFireBase(db: FirebaseFirestore) {
        db.collection("skillAndSkillsCrossRef").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val crossRef = SkillAndSkillCrossRef(
                        entry.data.getValue("skillId").toString(),
                        entry.data.getValue("childId").toString(),
                        entry.data.getValue("amount").toString().toInt()
                    )
                    viewModel.viewModelScope.launch {
                        viewModel.insertSkillAndSkillCrossRef(crossRef)
                    }

                }
            }
        }
    }

    private fun getSkillsFromFireBase(db: FirebaseFirestore, fbStorage: FirebaseStorage) {
        db.collection("skills").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val desc = entry.data.getValue("description").toString()
                    val type = entry.data.getValue("type").toString()
                    val pictureRef = fbStorage.reference.child("skillImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.nothing)
                    val skill = Skill(id,name,desc,bitmap,type)
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModel.viewModelScope.launch{
                                skill.skillImage = getBitmap(it)
                                viewModel.insertSkillToDatabase(skill)
                            }
                        }
                        .addOnFailureListener {
                            viewModel.viewModelScope.launch {
                                viewModel.insertSkillToDatabase(skill)
                            }
                        }
                }
            }
        }
    }

    private suspend fun getBitmap(source: Uri): Bitmap {
        val loading = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext()).data(source).build()
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


}