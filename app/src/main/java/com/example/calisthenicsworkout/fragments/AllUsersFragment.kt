package com.example.calisthenicsworkout.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
import com.example.calisthenicsworkout.adapters.UsersListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.databinding.FragmentAllUsersBinding
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory

class AllUsersFragment : Fragment() {

    private lateinit var binding: FragmentAllUsersBinding
    private lateinit var viewModel: SkillViewModel
    private lateinit var viewModelFactory: SkillViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_users,container,false)
        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = SkillViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(SkillViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = UsersListAdapter(UsersListAdapter.UserListener {
//               userId -> viewModel.onTrainingClicked(trainingId)
        })
        val manager = LinearLayoutManager(activity)
        binding.allUsersRecyclerView.layoutManager = manager
        binding.allUsersRecyclerView.adapter = adapter

        viewModel.getAllUsers(requireContext())

        viewModel.allUsers.observe(viewLifecycleOwner,{
            val list = mutableListOf<User>()
            it.forEach { user->
                list.add(user)
            }
            adapter.submitList(list)
        })


        return binding.root
    }


}