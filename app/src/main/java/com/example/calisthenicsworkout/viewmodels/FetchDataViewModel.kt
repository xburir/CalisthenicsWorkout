package com.example.calisthenicsworkout.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabaseDao
import com.example.calisthenicsworkout.database.entities.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FetchDataViewModel(val database: SkillDatabaseDao, application: Application): AndroidViewModel(application){



    val finished = MutableLiveData("nothing")

    private val db = FirebaseFirestore.getInstance()
    private val fbStorage = FirebaseStorage.getInstance()
    private val fbAuth = FirebaseAuth.getInstance()

    fun readFireStoreData(activity: Activity){
        val context = getApplication<Application>().applicationContext
        finished.value = "Starting"
        val skills = mutableListOf<Skill>()
        val trainings = mutableListOf<Training>()
        val exercises = mutableListOf<Exercise>()
        val skillAndSkillCrossRefs = mutableListOf<SkillAndSkillCrossRef>()
        val userAndSkillCrossRefs = mutableListOf<UserAndSkillCrossRef>()
        val user = User(FirebaseAuth.getInstance().currentUser!!.uid,"","")



        getUser(user)

        getSkillsFromFireBase(skills,context)
        getPredefinedTrainings(trainings,context)
        getUsersTrainings(user,trainings,context,skills,exercises)
        getSkillsAndSkillCrossRefFromFireBase(skillAndSkillCrossRefs)
        getUserAndSkillCrossRefFromFireBase(activity,userAndSkillCrossRefs,skills, user,trainings,exercises,skillAndSkillCrossRefs)






    }

    private fun addToDatabase(activity: Activity, skills: MutableList<Skill>, trainings: MutableList<Training>, exercises: MutableList<Exercise>, skillAndSkillCrossRefs: MutableList<SkillAndSkillCrossRef>, userAndSkillCrossRefs: MutableList<UserAndSkillCrossRef>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){

                activity.runOnUiThread { finished.value = "Adding skills to database"  }
                skills.forEach {
                    database.insert(it)

                }

                activity.runOnUiThread { finished.value = "Adding trainings to database" }
                trainings.forEach {
                    database.insertTraining(it)
                }

                activity.runOnUiThread { finished.value = "Adding exercises to database" }
                exercises.forEach {
                    database.insertExercise(it)
                }

                activity.runOnUiThread {finished.value = "Adding before skills to database" }
                skillAndSkillCrossRefs.forEach {
                    database.insertSkillAndSkillCrossRef(it)
                }

                activity.runOnUiThread {finished.value = "Adding liked skills to database" }
                userAndSkillCrossRefs.forEach {
                    database.insertUserAndSkillCrossRef(it)
                }

                activity.runOnUiThread {finished.value = "All done" }

            }
        }
    }

    private fun getSkillsFromFireBase(skills: MutableList<Skill>,context: Context) {
        finished.value = "Downloading Skills"
        db.collection("skills").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val desc = entry.data.getValue("description").toString()
                    val type = entry.data.getValue("type").toString()
                    val pictureRef = fbStorage.reference.child("skillImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                    val skill = Skill(id,name,desc,bitmap,type)
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModelScope.launch{
                                skill.skillImage = getBitmap(it,context)
                                skills.add(skill)
                            }
                        }
                        .addOnFailureListener {
                            skills.add(skill)
                        }
                }
            }
        }
    }

    private fun getExercisesForTrainings(trainings: MutableList<Training>, skills: MutableList<Skill>, exercises: MutableList<Exercise>, db: FirebaseFirestore) {
        finished.value = "Downloading Exercises"
        trainings.forEach { training ->
            db.collection("exercises").whereEqualTo("trainingId",training.id).get().addOnCompleteListener{
                if(it.isSuccessful){
                    for(entry in it.result!!){
                        val skillId = entry.data.getValue("skillId").toString()
                        val order = entry.data.getValue("order").toString().toInt()
                        val trainingId = entry.data.getValue("trainingId").toString()
                        val reps = entry.data.getValue("reps").toString()
                        val sets = entry.data.getValue("sets").toString()
                        skills.forEach { skillInAllSkills ->
                            if(skillInAllSkills.skillId == skillId){
                                val exercise = Exercise(trainingId,skillId,sets,reps,skillInAllSkills.skillImage,skillInAllSkills.skillName,order)
                                exercises.add(exercise)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUser(user: User) {
        finished.value = "Getting User Info"
        db.collection("users").document(fbAuth.currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                user.userEmail =  it.result!!.data?.getValue("userEmail").toString()
                user.userFullName = it.result!!.data?.getValue("userFullName").toString()

            }
        }

    }

    private fun checkIfNewSkillsWereAdded(skills: MutableList<Skill>, userAndSkillCrossRefs: MutableList<UserAndSkillCrossRef>, user: User, db: FirebaseFirestore, activity: Activity, trainings: MutableList<Training>, exercises: MutableList<Exercise>, skillAndSkillCrossRefs: MutableList<SkillAndSkillCrossRef>) {
        finished.value = "Checking if new skills were added"
        skills.forEach { skillInAllSkills ->
            var crossRefFound = false
            userAndSkillCrossRefs.forEach{ userSkillCrossRef ->
                if(userSkillCrossRef.skillId == skillInAllSkills.skillId){
                    crossRefFound = true
                }
                if(!crossRefFound){
                    val userId = user.userId
                    val skillId = skillInAllSkills.skillId
                    val liked = false
                    val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                    userAndSkillCrossRefs.add(crossRef)

                    val mappedThing: MutableMap<String,Any> = HashMap()
                    mappedThing["skillId"] = skillId
                    mappedThing["userId"] = userId
                    mappedThing["liked"] = liked
                    db.collection("userAndSkillCrossRef").add(mappedThing)
                }
            }
        }
        addToDatabase(activity,skills,trainings,exercises,skillAndSkillCrossRefs,userAndSkillCrossRefs)
    }

    private fun getUsersTrainings(user: User, trainings: MutableList<Training>, context: Context, skills: MutableList<Skill>, exercises: MutableList<Exercise>) {
        finished.value = "Downloading user's trainings"
        db.collection("trainings").whereEqualTo("owner",user.userId).get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                    val training = Training(name,target,id,user.userId,bitmap,numberOfExercises)
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                training.image = getBitmap(it,context)
                                trainings.add(training)
                            }
                        }
                        .addOnFailureListener {
                            trainings.add(training)
                        }


                }
            }
        }
        getExercisesForTrainings(trainings,skills,exercises,db)
    }

    private fun getPredefinedTrainings(trainings: MutableList<Training>,context: Context) {
        finished.value = "Downloading trainings"
        db.collection("trainings").whereEqualTo("owner","admin").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val id = entry.id
                    val name = entry.data.getValue("name").toString()
                    val target = entry.data.getValue("target").toString()
                    val numberOfExercises = entry.data.getValue("numberOfExercises").toString().toInt()
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.nothing)
                    val training = Training(name,target,id,"admin",bitmap,numberOfExercises)
                    val pictureRef = fbStorage.reference.child("trainingImages").child("$id.png")
                    pictureRef.downloadUrl
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                training.image = getBitmap(it,context)
                                trainings.add(training)
                            }
                        }
                        .addOnFailureListener {
                            trainings.add(training)
                        }

                }
            }
        }
    }

    private fun getUserAndSkillCrossRefFromFireBase(activity: Activity, userAndSkillCrossRefs: MutableList<UserAndSkillCrossRef>, skills: MutableList<Skill>, user: User,
    trainings: MutableList<Training>,exercises: MutableList<Exercise>,skillAndSkillCrossRefs: MutableList<SkillAndSkillCrossRef>) {
        finished.value = "Downloading info about liked skills"
        db.collection("userAndSkillCrossRef").whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                for (entry in it.result!!){
                    val userId = entry.data.getValue("userId").toString()
                    val skillId = entry.data.getValue("skillId").toString()
                    val liked = entry.data.getValue("liked").toString().toBooleanStrict()
                    val crossRef = UserAndSkillCrossRef(userId,skillId,liked)
                    userAndSkillCrossRefs.add(crossRef)
                }
            }
        }
        checkIfNewSkillsWereAdded(skills, userAndSkillCrossRefs, user , db,activity,trainings, exercises, skillAndSkillCrossRefs)
    }

    private fun getSkillsAndSkillCrossRefFromFireBase(skillAndSkillCrossRefs: MutableList<SkillAndSkillCrossRef>) {
        finished.value = "Downloading before skills"
        db.collection("skillAndSkillsCrossRef").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val crossRef = SkillAndSkillCrossRef(
                        entry.data.getValue("skillId").toString(),
                        entry.data.getValue("childId").toString(),
                        entry.data.getValue("amount").toString().toInt()
                    )
                    skillAndSkillCrossRefs.add(crossRef)
                }
            }
        }
    }



    private suspend fun getBitmap(source: Uri, context: Context): Bitmap {
        val loading = ImageLoader(context)
        val request = ImageRequest.Builder(context).data(source).build()
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