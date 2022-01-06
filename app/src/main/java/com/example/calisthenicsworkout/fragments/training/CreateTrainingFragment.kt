package com.example.calisthenicsworkout.fragments.training

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.calisthenicsworkout.util.BitmapUtil
import com.google.firebase.storage.FirebaseStorage


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
        val exerciseList = mutableListOf<Exercise>()
        val training = Training("undefined","undefined",key, fb.currentUser!!.uid ,
            Uri.parse("android.resource://com.example.calisthenicsworkout/drawable/default_training_pic"),0)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    binding.imageChooseInput.setText(it.toString())
                }
            }
        }

        binding.imageChooseInput.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

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
            val imgUrl = binding.imageChooseInput.text.toString()
            if (name.isNotEmpty()){
                if(exerciseList.isNotEmpty()){
                    training.name = name
                    training.target = target
                    viewModel.saveTraining(training,requireContext(),imgUrl,exerciseList)


                    findNavController().navigate(
                        CreateTrainingFragmentDirections.actionCreateTrainingFragmentToAllTrainingsFragment()
                    )

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



    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
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



}