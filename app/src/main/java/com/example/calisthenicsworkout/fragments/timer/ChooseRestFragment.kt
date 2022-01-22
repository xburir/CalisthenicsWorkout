package com.example.calisthenicsworkout.fragments.timer

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
import com.example.calisthenicsworkout.databinding.FragmentChooseRestBinding
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory
import java.lang.Exception


class ChooseRestFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel;
    private lateinit var viewModelFactory: TimerViewModelFactory;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentChooseRestBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_choose_rest, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = TimerViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(TimerViewModel::class.java)


        viewModel.training.observe(viewLifecycleOwner,{
           if( it.type == "circular"){
               binding.circularSetsInput.visibility = View.VISIBLE
               binding.betweenExercisesInput.visibility = View.GONE
           }else{
               binding.circularSetsInput.visibility = View.GONE
               binding.betweenExercisesInput.visibility = View.VISIBLE
           }
        })




        binding.setButton.setOnClickListener {
            val betweenSets = binding.betweenSetsInput.text.toString()
            val sets = binding.circularSetsInput.text.toString()
            val betweenExercises = binding.betweenExercisesInput.text.toString()

            if(viewModel._training.type == "circular"){
                if(checkInputs(betweenSets,sets)){
                    viewModel.timeBetweenExercises = 0L
                    viewModel._training.numberOfSets = sets
                    inputsOk(betweenSets)
                }
            }else{
                if(checkInputs(betweenExercises,betweenSets)){
                    viewModel.timeBetweenExercises =betweenExercises.toLong()
                    inputsOk(betweenSets)
                }
            }
            binding.betweenSetsInput.setText("")
            binding.betweenExercisesInput.setText("")
            binding.circularSetsInput.setText("")

        }

       return binding.root
    }

    private fun inputsOk(betweenSets: String) {
        viewModel.timeBetweenSets = betweenSets.toLong()
        findNavController().navigate(
            ChooseRestFragmentDirections.actionChooseRestFragmentToCounterFragment()
        )
        viewModel.prepareExercises()
    }

    private fun checkInputs(text: String, text1: String): Boolean{
        return try {
            val exercises = text.toInt()
            val sets = text1.toInt()
            if(exercises>0 && sets>0){
                true
            }else{
                Toast.makeText(context,"Numbers must be more than 0", Toast.LENGTH_SHORT).show()
                false
            }
        }catch (e: Exception){
            Toast.makeText(context,"Invalid number format", Toast.LENGTH_SHORT).show()
            false
        }
    }


}