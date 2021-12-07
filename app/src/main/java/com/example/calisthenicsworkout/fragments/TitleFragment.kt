package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentTitleBinding
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.database.entities.SkillAndSkillCrossRef
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import androidx.databinding.DataBindingUtil as DataBindingUtil1

class TitleFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding: FragmentTitleBinding = DataBindingUtil1.inflate(inflater,
            R.layout.fragment_title, container, false)

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
                    TitleFragmentDirections.actionTitleFragmentToSkillFragment(skill)
                )
            }
        })


        //sets a click listener to a button that then does an action
        binding.searchButton.setOnClickListener {
            Toast.makeText(context,binding.searchBar.text.toString(),Toast.LENGTH_SHORT).show()
        }



        // create menu resource
        setHasOptionsMenu(true)


        readFireStoreData()

        return binding.root


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    fun saveFireStore(crossRef: SkillAndSkillCrossRef){
        val db = FirebaseFirestore.getInstance()
        val mappedThing: MutableMap<String,Any> = HashMap()
        mappedThing["skillId"] = crossRef.skillId
        mappedThing["childId"] = crossRef.childSkillId
        mappedThing["amount"] = crossRef.minAmount

        db.collection("skillAndSkillsCrossRef").add(mappedThing)
            .addOnSuccessListener {
                Log.i("Debug","added succesfully")
            }
            .addOnFailureListener{
                Log.i("Debug","not added")
            }
    }

    private fun readFireStoreData(){
        val db = FirebaseFirestore.getInstance()
        db.collection("skills").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val skill = Skill(
                        entry.id,
                        entry.data.getValue("name").toString(),
                        entry.data.getValue("decription").toString()
                    )
                    viewModel.addSkillToDatabase(skill)
                }
            }
        }
        db.collection("skillAndSkillsCrossRef").get().addOnCompleteListener{
            if(it.isSuccessful){
                for(entry in it.result!!){
                    val crossRef = SkillAndSkillCrossRef(
                        entry.data.getValue("skillId").toString(),
                        entry.data.getValue("childId").toString(),
                        entry.data.getValue("amount").toString().toInt()
                    )
                    viewModel.addSkillAndSkillCrossRef(crossRef)
                }
            }
        }

    }

}