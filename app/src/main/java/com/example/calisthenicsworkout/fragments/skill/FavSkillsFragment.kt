package com.example.calisthenicsworkout.fragments.skill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.FragmentFavSkillsBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavSkillsFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel;
    private lateinit var viewModelFactory: SkillViewModelFactory;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentFavSkillsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_fav_skills,container,false)




        val application = requireNotNull(this.activity).application;
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)

        binding.skillViewModel = viewModel;
        binding.lifecycleOwner = this;


        val adapter = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = GridLayoutManager(activity, 3)
        binding.favSkillsList.layoutManager = manager
        binding.favSkillsList.adapter = adapter



        viewModel.userSkillCrossRefs.observe(viewLifecycleOwner,{
            viewModel.viewModelScope.launch {
                withContext(Dispatchers.IO){
                    it?.let{ userSkillCrossRefs ->
                        val skillsList = arrayListOf<Skill>()
                        userSkillCrossRefs.forEach { userAndSkillCrossRef ->
                            if(userAndSkillCrossRef.liked){
                                val skill = viewModel.database.getSkill(userAndSkillCrossRef.skillId)
                                skillsList.add(skill)
                            }
                        }
                        if(isAdded){
                            requireActivity().runOnUiThread {
                                adapter.submitList(skillsList)
                            }
                        }
                    }
                }
            }

        })

        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill ->
            skill?.let {
                this.findNavController().navigate(
                    FavSkillsFragmentDirections.actionFavSkillsFragmentToSkillFragment(
                        skill
                    )
                )
            }
        })

        return binding.root
    }

}