package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.FragmentSkillBinding
import com.example.calisthenicsworkout.databinding.FragmentTrainingBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainingFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel;
    private lateinit var viewModelFactory: SkillViewModelFactory;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentTrainingBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_training,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this





        val adapter = ExerciseListAdapter(ExerciseListAdapter.ExerciseListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter



        changeTrainingOnFragment(binding,viewModel.lastViewedTrainingId,adapter)


        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill->
            skill?.let {
                this.findNavController().navigate(
                    TrainingFragmentDirections.actionTrainingFragmentToSkillFragment(skill)
                )
            }
        })


        return binding.root
    }

    private fun changeTrainingOnFragment(binding: FragmentTrainingBinding, training: String, adapter: ExerciseListAdapter) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                binding.training = viewModel.database.getTraining(training)
            }
        }

        viewModel.database.getExercisesOfTraining(viewModel.lastViewedTrainingId).observe(viewLifecycleOwner,{  exercises ->
            val exerciseList = arrayListOf<Exercise>()
            exercises.forEach { exercise ->
                if(exercise.trainingId == viewModel.lastViewedTrainingId) {
                    exerciseList.add(exercise)
                }
            }
            adapter.submitList(exerciseList)
            viewModel.onTrainingNavigated()
        })

    }
}