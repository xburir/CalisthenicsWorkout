package com.example.calisthenicsworkout.fragments.training

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.ExerciseListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentCreateTrainingBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.example.calisthenicsworkout.viewmodels.TrainingViewModel
import com.example.calisthenicsworkout.viewmodels.TrainingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception


class CreateTrainingFragment : Fragment() {

    private lateinit var viewModel: TrainingViewModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentCreateTrainingBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_create_training,container,false)

        val application = requireNotNull(this.activity).application;
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModel = ViewModelProvider(requireActivity(),
            TrainingViewModelFactory(dataSource,application)
        ).get(TrainingViewModel::class.java)
        binding.lifecycleOwner = this;










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



        binding.saveTrainingButton.setOnClickListener{
            val name = binding.trainingNameInput.text.toString()
            val target = binding.targetInput.text.toString()
            val imgUrl = binding.imageChooseInput.text.toString()
            viewModel.saveTraining(name,target,imgUrl,requireContext())
            hideKeyBoard()
        }

        viewModel.finished.observe(viewLifecycleOwner,{
            if(it){
                this.findNavController().navigate(
                    CreateTrainingFragmentDirections.actionCreateTrainingFragmentToMyTrainingsFragment()
                )
            }

        })



        return binding.root
    }



    private fun hideKeyBoard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, 0)
    }












}