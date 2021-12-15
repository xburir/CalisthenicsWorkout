package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth


class SkillFragment : Fragment()  {

    private lateinit var viewModel: SkillViewModel;
    private lateinit var viewModelFactory: SkillViewModelFactory;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Inflate the layout for this fragment
        val binding: FragmentSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_skill,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.skillViewModel = viewModel;
        binding.lifecycleOwner = this


        // create menu resource
        setHasOptionsMenu(true)

        val managerBefore = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false)
        val adapterBefore = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        binding.beforeSkills.adapter = adapterBefore
        binding.beforeSkills.layoutManager = managerBefore

        val managerAfter = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false)
        val adapterAfter = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        binding.afterSkills.adapter = adapterAfter
        binding.afterSkills.layoutManager = managerAfter


        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill ->
            skill?.let {
                this.findNavController().navigate(
                    SkillFragmentDirections.actionSkillFragmentSelf(it)
                )
                viewModel.onSkillNavigated()
            }
        })
        changeSkillOnFragment(binding,viewModel.lastViewedSkillId,adapterBefore,adapterAfter)
        return binding.root
    }

    private fun changeSkillOnFragment(
        binding: FragmentSkillBinding,
        skill: String,
        adapterBefore: SkillListAdapter,
        adapterAfter: SkillListAdapter
        ) {
        viewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                binding.skill = viewModel.database.getSkill(skill)
                binding.skillViewModel = viewModel
                val beforeSkills = viewModel.database.getALlBeforeSkills(skill)
                val afterSkills = viewModel.database.getALlAfterSkills(skill)

                beforeSkills.forEach { skillInList ->
                    if(skillInList.skillType == "reps"){
                        skillInList.skillName = skillInList.skillName + " "+ viewModel.database.getCrossRefAmount(skill,skillInList.skillId).toString()+"x"
                    }else{
                        skillInList.skillName = skillInList.skillName + " "+ viewModel.database.getCrossRefAmount(skill,skillInList.skillId).toString()+"s"
                    }
                }
                if(isAdded){
                    requireActivity().runOnUiThread {
                        if (beforeSkills.isNotEmpty()) {
                            binding.beforeSkillsHeader.text = "Skills to learn before this one:"
                        } else{
                            binding.beforeSkillsHeader.text = ""
                        }
                        adapterBefore.submitList(beforeSkills)
                        if (afterSkills.isNotEmpty()) {
                            binding.afterSkillsHeader.text = "Skills which can be learned after this one:"
                        } else {
                            binding.afterSkillsHeader.text = ""
                        }
                        adapterAfter.submitList(afterSkills)
                    }
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_skill,menu)
        val item = menu[1]
        item.setIcon(R.drawable.like)
        viewModel.userSkillCrossRefs.observe(viewLifecycleOwner,{
            it?.let{ list ->
                list.forEach { userSkillCrossRef ->
                    if(userSkillCrossRef.skillId == viewModel.lastViewedSkillId){
                        if(userSkillCrossRef.liked){
                            item.setIcon(R.drawable.liked)
                        }
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.toString() == "About"){
            NavigationUI.onNavDestinationSelected(item,requireView().findNavController())
        }
        if (item.toString() == "Like"){
            var likeed = false
            viewModel.userSkillCrossRefs.observe(viewLifecycleOwner,{
                it?.let{ list ->
                    list.forEach { userAndSkillCrossRef ->
                        if(userAndSkillCrossRef.skillId == viewModel.lastViewedSkillId){
                            if(userAndSkillCrossRef.liked){
                                likeed = true
                            }
                        }
                    }
                }
            })
            val fireAuth = FirebaseAuth.getInstance()
            if(likeed){
                viewModel.userAndSkillCrossRef(fireAuth.currentUser!!.uid,viewModel.lastViewedSkillId,"setUnliked")
                item.setIcon(R.drawable.like)
                Toast.makeText(context,"Unliked",Toast.LENGTH_SHORT).show()
            }else{
                viewModel.userAndSkillCrossRef(fireAuth.currentUser!!.uid,viewModel.lastViewedSkillId,"setLiked")
                item.setIcon(R.drawable.liked)
                Toast.makeText(context,"Liked",Toast.LENGTH_SHORT).show()

            }

        }
        return super.onOptionsItemSelected(item)
    }


}