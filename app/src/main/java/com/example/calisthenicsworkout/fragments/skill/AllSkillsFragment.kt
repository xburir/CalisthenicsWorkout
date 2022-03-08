package com.example.calisthenicsworkout.fragments.skill

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.FetchDataDialogBinding
import com.example.calisthenicsworkout.databinding.FilterSkillsDialogBinding
import com.example.calisthenicsworkout.databinding.FragmentAllSkillsBinding
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
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

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = SkillListAdapter(SkillListAdapter.SkillListener {
            skillId -> viewModel.onSkillClicked(skillId)
        })
        val manager = GridLayoutManager(activity, 3)
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.adapter = adapter



        binding.filterButton.setOnClickListener{
            val dialog = Dialog(requireContext())
            val dialogBinding = FilterSkillsDialogBinding.inflate(LayoutInflater.from(requireContext()))
            dialog.setContentView(dialogBinding.root)
            dialog.setCancelable(false)
            dialog.show()

            dialogBinding.cancelFilterButton.setOnClickListener {
                dialog.dismiss()
            }

            setSavedFilterSettings(dialogBinding)

            dialogBinding.applyFilterButton.setOnClickListener {
                dialog.dismiss()
                saveFilterSettings(dialogBinding)
                filterSkills(adapter,binding.searchBar.text.toString().uppercase())
            }

        }

        filterSkills(adapter,binding.searchBar.text.toString().uppercase())



        viewModel.chosenSkillId.observe(viewLifecycleOwner,{ skill ->
            skill?.let {
                this.findNavController().navigate(
                    AllSkillsFragmentDirections.actionTitleFragmentToSkillFragment(
                        skill
                    )
                )
            }
        })



        binding.searchBar.addTextChangedListener {
            val searched = binding.searchBar.text.toString().uppercase()
            filterSkills(adapter,searched)
        }


        return binding.root


    }

    private fun filterSkills(adapter: SkillListAdapter,text: String) {

        val listAfterDiffFilter = mutableListOf<Skill>()
        val listAfterTargetFilter = mutableListOf<Skill>()
        val listAfterTextFilter = mutableListOf<Skill>()

        viewModel.allSkills.observe(viewLifecycleOwner, { skillList->

            var bits = PrefUtil.getFilterSetting(requireContext())
            if(bits.isNullOrBlank()){
                bits = "11111111111"
            }


            skillList.forEach { skill->
                if( (skill.difficulty == 1 && bits[0] == '1') ||
                    (skill.difficulty == 2 && bits[1] == '1') ||
                    (skill.difficulty == 3 && bits[2] == '1') ||
                    (skill.difficulty == 4 && bits[3] == '1') ||
                    (skill.difficulty == 5 && bits[4] == '1')){
                        listAfterDiffFilter.add(skill)
                }
            }

            listAfterDiffFilter.forEach { skill->
                if((skill.target.contains("abs") && bits[5] == '1') ||
                    (skill.target.contains("back") && bits[6] == '1') ||
                    (skill.target.contains("arms") && bits[7] == '1') ||
                    (skill.target.contains("legs") && bits[8] == '1') ||
                    (skill.target.contains("chest") && bits[9] == '1') ||
                    (skill.target.contains("shoulders") && bits[10] == '1')){
                        listAfterTargetFilter.add(skill)
                }
            }


            listAfterTargetFilter.sortBy{item->item.difficulty}

            listAfterTargetFilter.forEach { skill ->
                val name = skill.skillName.uppercase()
                val searched = text.uppercase()
                if(name.contains(searched)){
                    listAfterTextFilter.add(skill)
                }
            }

            adapter.submitList(listAfterTextFilter)

        })


    }

    private fun saveFilterSettings(dialogBinding: FilterSkillsDialogBinding) {
        val bits = "00000000000".toCharArray()

        if(dialogBinding.veryEasyChecBoxFilter.isChecked){bits[0] = '1'} else {bits[0] = '0'}
        if(dialogBinding.easyCheckBoxFilter.isChecked){bits[1] = '1'} else {bits[1] = '0'}
        if(dialogBinding.mediumCheckBoxFilter.isChecked){bits[2] = '1'} else {bits[2] = '0'}
        if(dialogBinding.hardCheckBoxFilter.isChecked){bits[3] = '1'} else {bits[3] = '0'}
        if(dialogBinding.veryHardCheckBoxFilter.isChecked){bits[4] = '1'} else {bits[4] = '0'}
        if(dialogBinding.absCheckBoxFilter.isChecked){bits[5] = '1'} else {bits[5] = '0'}
        if(dialogBinding.backCheckBoxFilter.isChecked){bits[6] = '1'} else {bits[6] = '0'}
        if(dialogBinding.armsCheckBoxFilter.isChecked){bits[7] = '1'} else {bits[7] = '0'}
        if(dialogBinding.legCheckBoxFilter.isChecked){bits[8] = '1'} else {bits[8] = '0'}
        if(dialogBinding.chestCheckBoxFilter.isChecked){bits[9] = '1'} else {bits[9] = '0'}
        if(dialogBinding.shouldersCheckBoxFilter.isChecked){bits[10] = '1'} else {bits[10] = '0'}

        PrefUtil.setFilterSettings(bits.joinToString(""),requireContext())

    }

    private fun setSavedFilterSettings(dialogBinding: FilterSkillsDialogBinding) {
        var bits = PrefUtil.getFilterSetting(requireContext())
        if (bits.isNullOrBlank()){
            bits = "11111111111"
        }
        dialogBinding.veryEasyChecBoxFilter.isChecked = bits[0] == '1'
        dialogBinding.easyCheckBoxFilter.isChecked = bits[1] == '1'
        dialogBinding.mediumCheckBoxFilter.isChecked = bits[2] == '1'
        dialogBinding.hardCheckBoxFilter.isChecked = bits[3] == '1'
        dialogBinding.veryHardCheckBoxFilter.isChecked = bits[4] == '1'
        dialogBinding.absCheckBoxFilter.isChecked = bits[5] == '1'
        dialogBinding.backCheckBoxFilter.isChecked = bits[6] == '1'
        dialogBinding.armsCheckBoxFilter.isChecked = bits[7] == '1'
        dialogBinding.legCheckBoxFilter.isChecked = bits[8] == '1'
        dialogBinding.chestCheckBoxFilter.isChecked = bits[9] == '1'
        dialogBinding.shouldersCheckBoxFilter.isChecked = bits[10] == '1'



    }


}