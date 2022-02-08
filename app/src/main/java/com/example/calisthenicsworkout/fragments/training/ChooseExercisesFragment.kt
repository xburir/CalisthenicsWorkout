package com.example.calisthenicsworkout.fragments.training

import android.content.Context
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentChooseExercisesBinding
import com.example.calisthenicsworkout.viewmodels.TrainingViewModel
import com.example.calisthenicsworkout.viewmodels.TrainingViewModelFactory
import java.lang.Exception

class ChooseExercisesFragment : Fragment() {

    private lateinit var binding: FragmentChooseExercisesBinding
    private lateinit var viewModel: TrainingViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_choose_exercises,container,false)

        val application = requireNotNull(this.activity).application;
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModel = ViewModelProvider(requireActivity(),
            TrainingViewModelFactory(dataSource,application)
        ).get(TrainingViewModel::class.java)
        binding.lifecycleOwner = this;

        val adapter = ExerciseListAdapter(ExerciseListAdapter.ExerciseListener {   })
        val manager = LinearLayoutManager(activity)
        binding.addedSkills.layoutManager = manager
        binding.addedSkills.adapter = adapter

        viewModel.allSkills.observe(viewLifecycleOwner,{
            val list = mutableListOf<String>()
            it.forEach { skill ->
                list.add(skill.skillName) }
            binding.skillOptions.setAdapter(ArrayAdapter(requireActivity(),android.R.layout.simple_dropdown_item_1line,list))
        })

        viewModel.listToDisplay.observe(viewLifecycleOwner,{
            adapter.submitList(it)
            if(it.isNotEmpty()){
                hideKeyBoard()
            }


        })


        if(viewModel.type == "circular"){
            binding.setsInput.visibility = View.GONE
        }



        binding.addSkillToTrainingButton.setOnClickListener{
            val nameOfSkill = binding.skillOptions.text.toString()
            val numberOfSets = binding.setsInput.text.toString()
            val numberOfReps = binding.repsInput.text.toString()

            if(viewModel.type == "circular"){
                if(checkReps(numberOfReps)){
                    viewModel.addExerciseToTraining(nameOfSkill,"0",numberOfReps,requireContext(),adapter,requireActivity())
                }
            }else{
                if(checkRepsAndSets(numberOfReps,numberOfSets)){
                    viewModel.addExerciseToTraining(nameOfSkill,numberOfSets,numberOfReps,requireContext(),adapter,requireActivity())
                }
            }



            binding.skillOptions.setText("")
            binding.repsInput.setText("")
            binding.setsInput.setText("")
        }

        binding.nexttButton.setOnClickListener {
            if (viewModel.exerciseList.isNotEmpty()){
                findNavController().navigate(
                    ChooseExercisesFragmentDirections.actionChooseExercisesFragmentToCreateTrainingFragment()
                )
            }else{
                Toast.makeText(context,"No exercises added",Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }

    private fun checkReps(numberOfReps: String): Boolean {
        return try {
            val repss = numberOfReps.toInt()
            if(repss > 0){
                true
            } else{
                Toast.makeText(context,"Numbers of reps must be more than 0",Toast.LENGTH_SHORT).show()
                false
            }
        }catch (e:Exception){
            Toast.makeText(context,"Invalid number format",Toast.LENGTH_SHORT).show()
            false
        }
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
        }catch (e: Exception){
            Toast.makeText(context,"Invalid number format",Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

    }
}