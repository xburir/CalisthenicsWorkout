package com.example.calisthenicsworkout.fragments.timer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.FragmentChooseRestBinding
import java.lang.Exception


class ChooseRestFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentChooseRestBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_choose_rest, container, false)

        binding.setButton.setOnClickListener {
            if(checkInputs(binding.betweenExercisesInput.text.toString(),binding.betweenSetsInput.text.toString())){
                findNavController().navigate(
                    ChooseRestFragmentDirections.actionChooseRestFragmentToCounterFragment(
                        binding.betweenSetsInput.text.toString().toInt(),
                        binding.betweenExercisesInput.text.toString().toInt()
                    )
                )
            }
            binding.betweenSetsInput.setText("")
            binding.betweenExercisesInput.setText("")

        }



       return binding.root
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