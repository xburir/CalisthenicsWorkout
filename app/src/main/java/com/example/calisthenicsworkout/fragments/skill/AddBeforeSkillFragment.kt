package com.example.calisthenicsworkout.fragments.skill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.databinding.FragmentAddBeforeSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.lang.Exception

class AddBeforeSkillFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentAddBeforeSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_before_skill,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        val args = AddBeforeSkillFragmentArgs.fromBundle(
            requireArguments()
        )


        viewModel.allSkills.observe(viewLifecycleOwner,{
            val list = mutableListOf<String>()
            it.forEach { skill ->
                if(args.skillId == skill.skillId){
                    binding.parentSkillName.text = skill.skillName
                }
                list.add(skill.skillName)
            }
            binding.beforeInput.setAdapter(ArrayAdapter(requireActivity(),android.R.layout.simple_dropdown_item_1line,list))
        })


        binding.button.setOnClickListener {
            if(binding.password.text.toString() != "secretpass"){
                Toast.makeText(context,"Bad password",Toast.LENGTH_SHORT).show()
            }else{
                if(checkInput(binding.amountInput.text.toString())){

                    val childSkillName = binding.beforeInput.text.toString()
                    var childId = ""
                    viewModel.allSkills.observe(viewLifecycleOwner,{
                        var found = false
                        it?.let { listOfSkills->
                            listOfSkills.forEach { skill->
                                if(skill.skillName == childSkillName){
                                    childId = skill.skillId
                                    found = true
                                }
                            }
                        }
                        if (!found){
                            Toast.makeText(context,"Child Skill not found",Toast.LENGTH_SHORT).show()
                        }else{
                            viewModel.database.getALlSkillCrossRefs().observe(viewLifecycleOwner,{
                                it?.let { listOfCrossRefs->
                                    var found = false
                                    listOfCrossRefs.forEach { crossRef->
                                        if(crossRef.skillId == args.skillId && crossRef.childSkillId == childId){
                                            Toast.makeText(context,"This relation is already added",Toast.LENGTH_SHORT).show()
                                            found = true
                                        }
                                    }
                                    if(!found){
                                        Toast.makeText(context,"Relation added",Toast.LENGTH_SHORT).show()
                                        val crossRef = SkillAndSkillCrossRef(args.skillId,childId,binding.amountInput.text.toString().toInt())
                                        viewModel.viewModelScope.launch {
                                            viewModel.insertSkillAndSkillCrossRef(crossRef)
                                        }

                                        val database = FirebaseFirestore.getInstance()
                                        val mappedCrossRef: MutableMap<String,Any> = HashMap()
                                        mappedCrossRef["amount"] = crossRef.minAmount
                                        mappedCrossRef["childId"] = crossRef.childSkillId
                                        mappedCrossRef["skillId"] = crossRef.skillId
                                        database.collection("skillAndSkillsCrossRef").add(mappedCrossRef)
                                    }
                                }
                            })
                        }
                    })

                }
            }


        }



        return binding.root
    }

    private fun checkInput(text: String):Boolean{
        return try {
            val amount = text.toInt()

            if(amount>0){
                true
            }else{
                Toast.makeText(context,"Amount number must be more than 0",Toast.LENGTH_SHORT).show()
                false
            }
        }catch (e: Exception){
            Toast.makeText(context,"Invalid number format",Toast.LENGTH_SHORT).show()
            false
        }
    }

}