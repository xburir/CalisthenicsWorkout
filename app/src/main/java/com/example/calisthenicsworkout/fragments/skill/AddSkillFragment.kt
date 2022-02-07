package com.example.calisthenicsworkout.fragments.skill

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentAddSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class AddSkillFragment : Fragment() {
    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentAddSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_skill,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        binding.difficultySeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p1){
                    1 -> {
                        binding.difficultyShowerInAddSkill.text = "very easy"
                    }
                    2 -> {
                        binding.difficultyShowerInAddSkill.text = "easy"
                    }
                    3 -> {
                        binding.difficultyShowerInAddSkill.text = "medium"
                    }
                    4 -> {
                        binding.difficultyShowerInAddSkill.text = "difficult"
                    }
                    5 -> {
                        binding.difficultyShowerInAddSkill.text = "very difficult"
                    }
                }

            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        binding.saveSkillButton.setOnClickListener {
            if (binding.passwordInput.text.toString() != "secretpass") {
                Toast.makeText(context, "Bad password", Toast.LENGTH_SHORT).show()
            } else {
                if (checkInput(binding.nameInput.text.toString(),  binding.descriptionINput.text.toString(),  binding.radioGroup)) {
                    val name = binding.nameInput.text.toString()
                    val desc = binding.descriptionINput.text.toString()
                    val target = ArrayList<String>()
                    val difficulty = binding.difficultySeekBar.progress

                    if(binding.absCheckBox.isChecked){
                        target.add("abs")
                    }
                    if(binding.shouldersCheckBox.isChecked){
                        target.add("shoulders")
                    }
                    if(binding.chestCheckBox.isChecked){
                        target.add("chest")
                    }
                    if(binding.legsCheckBox.isChecked){
                        target.add("legs")
                    }
                    if(binding.armsCheckBox.isChecked){
                        target.add("abs")
                    }
                    if(binding.backCheckBox.isChecked){
                        target.add("back")
                    }

                    if(binding.repsRadioButton.isChecked){
                        saveFireStore(name,"reps",desc,target,difficulty)
                    }
                    if (binding.timeRadioButton.isChecked)
                        saveFireStore(name,"time",desc,target,difficulty)
                    findNavController().navigate(
                        AddSkillFragmentDirections.actionAddSkillFragmentToSkillFragment(
                            viewModel.lastViewedSkillId
                        )
                    )

                }
            }
        }

        return binding.root
    }

    fun saveFireStore(name: String, type: String, desc: String, target: List<String>, difficulty: Int){
        val db = FirebaseFirestore.getInstance()
        val mappedThing: MutableMap<String,Any> = HashMap()
        mappedThing["name"] = name
        mappedThing["type"] = type
        mappedThing["description"] = desc
        mappedThing["target"] = target
        mappedThing["difficulty"] = difficulty
        db.collection("skills").add(mappedThing)
            .addOnSuccessListener {
                Log.i("Debug","added succesfully")
            }
            .addOnFailureListener{
                Log.i("Debug","not added")
            }
    }

    private fun checkInput(name: String,desc: String,radioGroup: RadioGroup):Boolean{
        if(name.isNotEmpty()){
            if(desc.isNotEmpty()){
                if(radioGroup.checkedRadioButtonId>0){
                    return true
                }else{
                    Toast.makeText(context,"Check skill type",Toast.LENGTH_SHORT).show()
                    return false
                }
            }else{
                Toast.makeText(context,"Fill out the description",Toast.LENGTH_SHORT).show()
                return false
            }
        }else{
            Toast.makeText(context,"Fill out the name",Toast.LENGTH_SHORT).show()
            return false
        }
    }

}