package com.example.calisthenicsworkout.fragments

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentTitleBinding
import com.example.calisthenicsworkout.SkillListAdapter
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

        val adapter = SkillListAdapter()
        binding.recyclerView.adapter = adapter
        viewModel.allSkills.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        //sets a click listener to a button that then does an action (changing the fragment)
        binding.searchButton.setOnClickListener { view: View ->
            var search : String = binding.searchBar.text.toString()
            viewModel.chosenSkill.value = search
            view.findNavController().navigate(
                TitleFragmentDirections.actionTitleFragmentToSkillFragment()
            )
        }






        // create menu resource
        // call setHasOptionsMenu(true)
        setHasOptionsMenu(true)





        return binding.root


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,requireView().findNavController()) || super.onOptionsItemSelected(item)
    }
}