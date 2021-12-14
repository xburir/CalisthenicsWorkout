package com.example.calisthenicsworkout.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.database.entities.UserAndSkillCrossRef
import com.example.calisthenicsworkout.databinding.FragmentAllSkillsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.databinding.DataBindingUtil as DataBindingUtil1

class AllSkillsFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding: FragmentAllSkillsBinding = DataBindingUtil1.inflate(inflater,
            R.layout.fragment_all_skills, container, false)

        Log.i("Debug","ViewModelProvider called")
        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.skillViewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = SkillListAdapter(SkillListAdapter.SkillListener {
            skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = GridLayoutManager(activity, 3)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter

        viewModel.allSkills.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        viewModel.chosenSkillId.observe(viewLifecycleOwner, Observer { skill ->
            skill?.let {
                this.findNavController().navigate(
                    AllSkillsFragmentDirections.actionTitleFragmentToSkillFragment(skill)
                )
            }
        })



        binding.searchBar.addTextChangedListener {
            val list = arrayListOf<Skill>()
            viewModel.allSkills.observe(viewLifecycleOwner,{
                it.forEach { skill ->
                    if(skill.skillName.contains(binding.searchBar.text.toString())){
                        list.add(skill)
                    }
                }
                adapter.submitList(list)
            })
        }






        return binding.root


    }





}