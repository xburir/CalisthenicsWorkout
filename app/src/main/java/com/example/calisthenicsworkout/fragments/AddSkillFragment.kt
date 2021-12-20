package com.example.calisthenicsworkout.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentAddSkillBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory

class AddSkillFragment : Fragment() {

    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentAddSkillBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add_skill,container,false)


        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application)
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.skillViewModel = viewModel
        binding.lifecycleOwner = this

        //sets a click listener to a button that then does an action (changing the fragment)
        binding.button.setOnClickListener{ view: View ->

        }



        return binding.root
    }


}