package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentTitleBinding
import androidx.databinding.DataBindingUtil as DataBindingUtil1

class TitleFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding: FragmentTitleBinding = DataBindingUtil1.inflate(inflater,
            R.layout.fragment_title, container, false)


        //sets a click listener to a button that then does an action (changing the fragment)
        binding.searchButton.setOnClickListener { view: View ->
            var search : String = binding.searchBar.text.toString()
            view.findNavController().navigate(
                TitleFragmentDirections.actionTitleFragmentToSkillFragment(
                    search
                )
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