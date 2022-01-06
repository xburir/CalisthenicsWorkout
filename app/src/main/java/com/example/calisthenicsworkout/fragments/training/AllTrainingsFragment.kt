package com.example.calisthenicsworkout.fragments.training

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentAllTrainingsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class AllTrainingsFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAllTrainingsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_all_trainings, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = TrainingListAdapter(TrainingListAdapter.TrainingListener {
                trainingId -> viewModel.onTrainingClicked(trainingId)
        })
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter

        viewModel.allTrainings.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        viewModel.chosenTrainingId.observe(viewLifecycleOwner, { training ->
            training?.let {
                this.findNavController().navigate(
                    AllTrainingsFragmentDirections.actionAllTrainingsFragmentToTrainingFragment()
                )
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_all_trainings,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.toString() == "Add Training"){
            val input = EditText(context)
            input.hint = "Enter Text"
            input.inputType = InputType.TYPE_CLASS_TEXT
            AlertDialog.Builder(context)
                .setTitle("Add Training")
                .setView(input)
                .setPositiveButton("OK") {_,_->
                    val id = input.text.toString()
                    viewModel.addSharedTraining(id,requireContext())
                }
                .setNegativeButton("Cancel",null)
                .show()
        }

        return super.onOptionsItemSelected(item)
    }




}