package com.example.calisthenicsworkout.fragments.skill

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calisthenicsworkout.MainActivity
import com.example.calisthenicsworkout.PhotoActivity
import com.example.calisthenicsworkout.VideoActivity
import com.example.calisthenicsworkout.adapters.TargetInSkillListAdapter
import com.google.firebase.auth.FirebaseAuth


class SkillFragment : Fragment()  {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Inflate the layout for this fragment
        val binding: FragmentSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_skill,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application)
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)




        // create menu resource
        setHasOptionsMenu(true)

        val managerBefore = GridLayoutManager(activity, 3)
        val adapterBefore = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        binding.beforeSkills.adapter = adapterBefore
        binding.beforeSkills.layoutManager = managerBefore

        val managerAfter = GridLayoutManager(activity, 3)
        val adapterAfter = SkillListAdapter(SkillListAdapter.SkillListener {
                skillId -> viewModel.onSkillClicked(skillId)
        })
        binding.afterSkills.adapter = adapterAfter
        binding.afterSkills.layoutManager = managerAfter




        binding.skillImageViewed.setOnClickListener{
            val intent = Intent(requireActivity(), PhotoActivity::class.java)
            intent.putExtra("folder","skillImages")
            intent.putExtra("id",viewModel.lastViewedSkillId)
            startActivity(intent)
        }

        viewModel.chosenSkillId.observe(viewLifecycleOwner, { skill ->
            skill?.let {
                this.findNavController().navigate(
                    SkillFragmentDirections.actionSkillFragmentSelf(
                        it
                    )
                )
                viewModel.onSkillNavigated()
            }
        })




        viewModel.finishedLoading.observe(viewLifecycleOwner,{
            if(it){
                val managerTargets = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
                val adapterTargets = TargetInSkillListAdapter(TargetInSkillListAdapter.ClickListener{ target ->
                    Toast.makeText(context,target,Toast.LENGTH_SHORT).show()
                })
                adapterTargets.submitList(viewModel.chosenSkill.target)
                binding.skillsTargetRecyclerViewer.adapter = adapterTargets
                binding.skillsTargetRecyclerViewer.layoutManager = managerTargets

                val actionBar = (activity as MainActivity).supportActionBar
                actionBar?.title = viewModel.chosenSkill.skillName


                binding.skill = viewModel.chosenSkill
                if (viewModel.beforeSkills.isNotEmpty()) {
                    binding.beforeSkillsHeader.visibility = View.VISIBLE
                    binding.beforeSkills.visibility = View.VISIBLE
                } else{
                    binding.beforeSkillsHeader.visibility = View.GONE
                    binding.beforeSkills.visibility = View.GONE
                }
                adapterBefore.submitList(viewModel.beforeSkills)
                if (viewModel.afterSkills.isNotEmpty()) {
                    binding.afterSkillsHeader.visibility = View.VISIBLE
                    binding.afterSkills.visibility = View.VISIBLE
                } else {
                    binding.afterSkillsHeader.visibility = View.GONE
                    binding.afterSkills.visibility = View.GONE
                }
                adapterAfter.submitList(viewModel.afterSkills)
            }
        })

        binding.beforeSkillsHeader.setOnClickListener {

            if(binding.beforeSkills.visibility == View.GONE){
                binding.beforeSkillsHeader.text = "Skills to learn before this one: (Click to collapse)"
                binding.beforeSkills.visibility = View.VISIBLE
            }else{
                binding.beforeSkillsHeader.text = "Skills to learn before this one: (Click to expand)"
                binding.beforeSkills.visibility = View.GONE
            }

        }

        binding.afterSkillsHeader.setOnClickListener {
            if(binding.afterSkills.visibility == View.GONE){
                binding.afterSkillsHeader.text = "Skills which can be learned after this one: (Click to collapse)"
                binding.afterSkills.visibility = View.VISIBLE
            }else{
                binding.afterSkillsHeader.text = "Skills which can be learned after this one: (Click to expand)"
                binding.afterSkills.visibility = View.GONE
            }
        }



        return binding.root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_skill,menu)

        viewModel.finishedLoading.observe(viewLifecycleOwner,{
            if(it){
                if(viewModel.chosenSkill.skillName.length > 18){
                    menu[3].setShowAsAction(0)
                }
            }
        })



        val item = menu[2]
        item.setIcon(android.R.drawable.btn_star_big_off)
        viewModel.userSkillCrossRefs.observe(viewLifecycleOwner,{
            it?.let{ list ->
                list.forEach { userSkillCrossRef ->
                    if(userSkillCrossRef.skillId == viewModel.lastViewedSkillId){
                        if(userSkillCrossRef.liked){
                            item.setIcon(android.R.drawable.btn_star_big_on)
                        }
                    }
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.toString() == "Video"){
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra("skillId",viewModel.lastViewedSkillId)
            startActivity(intent)
        }
        if(item.toString() == "Add Before Skill"){
            findNavController().navigate(
                SkillFragmentDirections.actionSkillFragmentToAboutFragment(viewModel.lastViewedSkillId)
            )
        }
        if(item.toString() == "Add Skill"){
            findNavController().navigate(
                SkillFragmentDirections.actionSkillFragmentToAddSkillFragment()

            )
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
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            if(likeed){
                viewModel.userAndSkillCrossRef(userId,viewModel.lastViewedSkillId,false)
                item.setIcon(android.R.drawable.btn_star_big_off)
                Toast.makeText(context,"Unliked",Toast.LENGTH_SHORT).show()
            }else{
                viewModel.userAndSkillCrossRef(userId,viewModel.lastViewedSkillId,true)
                item.setIcon(android.R.drawable.btn_star_big_on)
                Toast.makeText(context,"Liked",Toast.LENGTH_SHORT).show()

            }

        }
        return super.onOptionsItemSelected(item)
    }




}