package com.example.calisthenicsworkout.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentCreateTrainingBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

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

        val database = FirebaseFirestore.getInstance()
        val key =  getRandomString(20)
        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val exerciseList = mutableListOf<Exercise>()
        val training = Training("undefined","undefined",key, emptyBitmap)

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
                            if(skill.skillType == "reps"){
                                exercise = Exercise(key,skill.skillId ,numberOfSets,numberOfReps+" repetitions", skill.skillImage,skill.skillName)
                            }else{
                                exercise = Exercise(key,skill.skillId ,numberOfSets,numberOfReps+" seconds", skill.skillImage,skill.skillName)
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
                })
            }
        }





        return binding.root
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun checkRepsAndSets(reps:String, sets:String):Boolean{
        try {
            val repss = reps.toInt()
            val setss = sets.toInt()
            if(repss>0 && setss>0){
                return true
            }else{
                Toast.makeText(context,"Numbers of reps and sets must be more than 0",Toast.LENGTH_SHORT).show()
                return false
            }
        }catch (e:Exception){
            Toast.makeText(context,"Invalid number format",Toast.LENGTH_SHORT).show()
            return false
        }
    }

}