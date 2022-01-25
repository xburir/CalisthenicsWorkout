package com.example.calisthenicsworkout.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.UsersListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.databinding.FragmentAllUsersBinding
import com.example.calisthenicsworkout.databinding.ProgressDialogBinding
import com.example.calisthenicsworkout.viewmodels.ProfileViewModel
import com.example.calisthenicsworkout.viewmodels.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class AllUsersFragment : Fragment() {

    private lateinit var binding: FragmentAllUsersBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_all_users,container,false)
        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = ProfileViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(ProfileViewModel::class.java)
        binding.lifecycleOwner = this

        val adapter = UsersListAdapter(UsersListAdapter.UserListener {
            viewModel.setUser(it)
        })
        val manager = LinearLayoutManager(activity)
        binding.allUsersRecyclerView.layoutManager = manager
        binding.allUsersRecyclerView.adapter = adapter



        downloadingProgressShow()


        viewModel.allUsers.observe(viewLifecycleOwner,{
            val list = mutableListOf<User>()
            it.forEach { user->
                if(user.userId != FirebaseAuth.getInstance().currentUser!!.uid){
                    list.add(user)
                }
            }
            list.sortBy { item -> item.userFullName }
            adapter.submitList(list)
        })

        viewModel.chosenUser.observe(viewLifecycleOwner,{
            it?.let{
                findNavController().navigate(
                    AllUsersFragmentDirections.actionAllUsersFragmentToProfileFragment()
                )
            }
        })


        return binding.root
    }

    private fun downloadingProgressShow() {
        if(viewModel.downloadProgress.value != 100L){
            viewModel.getAllUsers()
        }
        val dialog = Dialog(requireContext())
        val dialogBinding = ProgressDialogBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()
        viewModel.downloadProgress.observe(viewLifecycleOwner,{ progress ->
            dialogBinding.progressBar5.progress = progress.toInt()
            dialogBinding.progressDialogPercent.text = "$progress%"
            dialogBinding.progressDialogTitle.text = "Getting Users"
            if(progress == 100L){
                dialog.dismiss()
            }
        })
    }


}