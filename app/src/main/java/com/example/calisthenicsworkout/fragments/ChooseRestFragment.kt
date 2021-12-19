package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentAllSkillsBinding
import com.example.calisthenicsworkout.databinding.FragmentChooseRestBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory


class ChooseRestFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentChooseRestBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_choose_rest, container, false)

        binding.setButton.setOnClickListener {
            findNavController().navigate(
                ChooseRestFragmentDirections.actionChooseRestFragmentToCounterFragment(
                    binding.betweenSetsInput.text.toString().toInt(),
                    binding.betweenExercisesInput.text.toString().toInt()
                )
            )
        }



       return binding.root
    }


}