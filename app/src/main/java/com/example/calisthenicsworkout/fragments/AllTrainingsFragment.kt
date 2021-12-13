package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentAllSkillsBinding
import com.example.calisthenicsworkout.databinding.FragmentAllTrainingsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory


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
                trainingId -> viewModel.onSkillClicked(trainingId)
        })
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter

        viewModel.allTrainings.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

        return binding.root
    }
}