package com.example.calisthenicsworkout.fragments.training

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentCreateTrainingBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.lang.Exception
import android.util.Base64
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File


class CreateTrainingFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateTrainingBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_create_training,container,false)

        val application = requireNotNull(this.activity).application;
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModel = ViewModelProvider(requireActivity(),SkillViewModelFactory(dataSource,application)).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this;

        val adapter = ExerciseListAdapter(ExerciseListAdapter.ExerciseListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = LinearLayoutManager(activity)
        binding.addedSkills.layoutManager = manager
        binding.addedSkills.adapter = adapter

        viewModel.allSkills.observe(viewLifecycleOwner,{
            val list = mutableListOf<String>()
            it.forEach { skill ->
                list.add(skill.skillName) }
            binding.skillOptions.setAdapter(ArrayAdapter(requireActivity(),android.R.layout.simple_dropdown_item_1line,list))
        })


        val fb = FirebaseAuth.getInstance()
        val key =  getRandomString(20)
        val emptyBitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.nothing)
        val exerciseList = mutableListOf<Exercise>()
        val training = Training("undefined","undefined",key, fb.currentUser!!.uid  ,emptyBitmap,0)

        binding.addSkillToTrainingButton.setOnClickListener{
            val nameOfSkill = binding.skillOptions.text.toString()
            val numberOfSets = binding.setsInput.text.toString()
            val numberOfReps = binding.repsInput.text.toString()
            if(checkRepsAndSets(numberOfReps,numberOfSets)){
                viewModel.allSkills.observe(viewLifecycleOwner,{
                    var found = false
                    var exercise: Exercise
                    it.forEach { skill->
                        if(skill.skillName == nameOfSkill){
                            found = true
                            training.numberOfExercises++
                            exercise = if(skill.skillType == "reps"){
                                if(numberOfReps == "1"){ Exercise(key,skill.skillId ,numberOfSets, "$numberOfReps repetition", skill.skillImage,skill.skillName,training.numberOfExercises) }
                                else{ Exercise(key,skill.skillId ,numberOfSets,"$numberOfReps repetitions", skill.skillImage,skill.skillName,training.numberOfExercises)  }
                            }else{
                                if (numberOfReps == "1"){  Exercise(key,skill.skillId ,numberOfSets,  "$numberOfReps second", skill.skillImage,skill.skillName,training.numberOfExercises)  }
                                else{ Exercise(key,skill.skillId ,numberOfSets, "$numberOfReps seconds", skill.skillImage,skill.skillName,training.numberOfExercises) }
                            }
                            exerciseList.add(exercise)
                        }
                    }
                    binding.skillOptions.setText("")
                    binding.repsInput.setText("")
                    binding.setsInput.setText("")
                    if(!found){
                        Toast.makeText(context,"Skill not found",Toast.LENGTH_SHORT).show()

                    }else{
                        adapter.submitList(exerciseList)
                    }
                    hideKeyBoard()
                })
            }
        }

        binding.saveTrainingButton.setOnClickListener{
            val name = binding.trainingNameInput.text.toString()
            val target = binding.targetInput.text.toString()
            val imgUrl = binding.imageUrlInput.text.toString()
            if (name.isNotEmpty()){
                if(exerciseList.isNotEmpty()){
                    viewModel.viewModelScope.launch {
                        training.name = name
                        training.target = target
                        try {
                            training.image = getBitmap(imgUrl)
                            saveTrainingImageToFireBaseStorage(training)
                        }catch (e:Exception){   }
                        viewModel.addTrainingToDatabase(training)
                        exerciseList.forEach {
                            viewModel.addExerciseToDatabase(it)
                        }
                        saveToFirestore(training,exerciseList)
                        viewModel.lastViewedTrainingId = training.id
                        findNavController().navigate(
                            CreateTrainingFragmentDirections.actionCreateTrainingFragmentToTrainingFragment(
                                training.id
                            )
                        )
                    }
                }else{ Toast.makeText(context,"Your exercises list is empty",Toast.LENGTH_SHORT).show()  }
            }else{ Toast.makeText(context,"Set the name of your training",Toast.LENGTH_SHORT).show() }
            hideKeyBoard()
        }

        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill->
            skill?.let {
                this.findNavController().navigate(
                    CreateTrainingFragmentDirections.actionCreateTrainingFragmentToSkillFragment(
                        skill
                    )
                )
            }
        })

        return binding.root
    }

    private fun saveTrainingImageToFireBaseStorage(training: Training) {
        val file = File(requireContext().cacheDir,"CUSTOM NAME")
        file.delete()
        file.createNewFile()
        val fileOutputStream = file.outputStream()
        val byteArrayOutputStream = ByteArrayOutputStream()
        training.image.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream)
        val bytearray = byteArrayOutputStream.toByteArray()
        fileOutputStream.write(bytearray)
        fileOutputStream.flush()
        fileOutputStream.close()
        byteArrayOutputStream.close()
        val urlka = file.toUri()
        FirebaseStorage.getInstance().reference.child("trainingImages").child(training.id+".png").putFile(urlka)

    }

    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }

    private fun saveToFirestore(training: Training, exerciseList: MutableList<Exercise>) {
        val database = FirebaseFirestore.getInstance()
        val mappedTraining: MutableMap<String,Any> = HashMap()
        mappedTraining["name"] = training.name
        mappedTraining["owner"] = training.owner
        mappedTraining["numberOfExercises"] = training.numberOfExercises
        mappedTraining["target"] = training.target
        database.collection("trainings").document(training.id).set(mappedTraining)
        exerciseList.forEach{
            val mappedExercise: MutableMap<String,Any> = HashMap()
            mappedExercise["reps"] = it.repetitions
            mappedExercise["sets"] = it.sets
            mappedExercise["order"] = it.order
            mappedExercise["skillId"] = it.skillId
            mappedExercise["trainingId"] = it.trainingId
            database.collection("exercises").add(mappedExercise)
        }

    }




    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun checkRepsAndSets(reps:String, sets:String):Boolean{
        return try {
            val repss = reps.toInt()
            val setss = sets.toInt()
            if(repss>0 && setss>0){
                true
            }else{
                Toast.makeText(context,"Numbers of reps and sets must be more than 0",Toast.LENGTH_SHORT).show()
                false
            }
        }catch (e:Exception){
            Toast.makeText(context,"Invalid number format",Toast.LENGTH_SHORT).show()
            false
        }
    }

    private suspend fun getBitmap(source: String): Bitmap {
        val loading = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext()).data(source).build()
        val result = (loading.execute(request) as SuccessResult).drawable
        return (result as BitmapDrawable).bitmap
    }

}