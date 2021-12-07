package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
//import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkillFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel;
    private lateinit var viewModelFactory: SkillViewModelFactory;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_skill,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.skillViewModel = viewModel;
        binding.lifecycleOwner = this;


        val manager = GridLayoutManager(activity, 3)
        val adapter = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        binding.recyclerViewViewed.adapter = adapter
        binding.recyclerViewViewed.layoutManager = manager


        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill ->
            skill?.let {
                viewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO){
                        binding.skill = viewModel.database.getSkill(skill)
                        adapter.submitList(viewModel.database.getALlBeforeSkills(skill))
                    }
                }
                viewModel.onSkillNavigated()
            }
        })







        return binding.root
    }

}