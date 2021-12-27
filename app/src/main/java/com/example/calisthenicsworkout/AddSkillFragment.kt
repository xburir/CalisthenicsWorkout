package com.example.calisthenicsworkout

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.databinding.FragmentAddBeforeSkillBinding
import com.example.calisthenicsworkout.databinding.FragmentAddSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.lang.Exception

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

        binding.saveSkillButton.setOnClickListener {
            if (binding.passwordInput.text.toString() != "secretpass") {
                Toast.makeText(context, "Bad password", Toast.LENGTH_SHORT).show()
            } else {
                if (checkInput(binding.nameInput.text.toString(),  binding.descriptionINput.text.toString(),  binding.radioGroup)) {
                    if(binding.repsRadioButton.isChecked){
                        saveFireStore(binding.nameInput.text.toString(),"reps",binding.descriptionINput.text.toString())
                    }
                    if (binding.timeRadioButton.isChecked)
                        saveFireStore(binding.nameInput.text.toString(),"time",binding.descriptionINput.text.toString())
                    findNavController().navigate(
                        AddSkillFragmentDirections.actionAddSkillFragmentToSkillFragment(viewModel.lastViewedSkillId)
                    )

                }
            }
        }

        return binding.root
    }

    fun saveFireStore(name: String, type: String, desc: String){
        val db = FirebaseFirestore.getInstance()
        val mappedThing: MutableMap<String,Any> = HashMap()
        mappedThing["name"] = name
        mappedThing["type"] = type
        mappedThing["description"] = desc
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