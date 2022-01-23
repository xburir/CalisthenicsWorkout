package com.example.calisthenicsworkout.fragments.training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentChooseTrainingTypeBinding
import com.example.calisthenicsworkout.viewmodels.TrainingViewModel
import com.example.calisthenicsworkout.viewmodels.TrainingViewModelFactory

class ChooseTrainingTypeFragment : Fragment() {

    private lateinit var binding : FragmentChooseTrainingTypeBinding
    private lateinit var viewModel: TrainingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_choose_training_type,container,false)

        val application = requireNotNull(this.activity).application;
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModel = ViewModelProvider(requireActivity(),TrainingViewModelFactory(dataSource,application)).get(TrainingViewModel::class.java)
        binding.lifecycleOwner = this;


        binding.nextButton.setOnClickListener {
            if(binding.circularTrainingButton.isChecked){
                viewModel.type = "circular"
                viewModel.training.type = "circular"
                navigate()
            }else if( binding.normalTrainingButton.isChecked){
                viewModel.type = "normal"
                viewModel.training.type = "resistance"
                navigate()
            }else{
                Toast.makeText(context,"Select a type",Toast.LENGTH_SHORT).show()
            }


        }

        return binding.root
    }

    private fun navigate() {
        viewModel.exerciseList.clear()
        findNavController().navigate(
            ChooseTrainingTypeFragmentDirections.actionChooseTrainingTypeFragment2ToChooseExercisesFragment()
        )
    }


}