package com.example.calisthenicsworkout.fragments.training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.FragmentMyTrainingsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory


class MyTrainingsFragment : Fragment() {
   private lateinit var binding: FragmentMyTrainingsBinding
    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_my_trainings,container,false)
        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this


        val adapter = TrainingListAdapter(TrainingListAdapter.TrainingListener {
                trainingId -> viewModel.onTrainingClicked(trainingId)
        })
        val manager = LinearLayoutManager(activity)
        binding.myTrainingsRecyclerView.layoutManager = manager
        binding.myTrainingsRecyclerView.adapter = adapter

        viewModel.allTrainings.observe(viewLifecycleOwner,{
            val list = mutableListOf<Training>()
            it.forEach { training ->
                if(training.owner != "admin"){
                    list.add(training)
                }
            }
            list.sortBy { item->item.name }
            adapter.submitList(list)
        })

        viewModel.chosenTrainingId.observe(viewLifecycleOwner, { training ->
            training?.let {
                this.findNavController().navigate(
                    MyTrainingsFragmentDirections.actionMyTrainingsFragmentToTrainingFragment()
                )
            }
        })


        return binding.root
    }

}