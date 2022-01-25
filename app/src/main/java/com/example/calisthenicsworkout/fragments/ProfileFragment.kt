package com.example.calisthenicsworkout.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.PhotoActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentProfileBinding
import com.example.calisthenicsworkout.viewmodels.ProfileViewModel
import com.example.calisthenicsworkout.viewmodels.ProfileViewModelFactory

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = ProfileViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(ProfileViewModel::class.java)
        binding.lifecycleOwner = this


        viewModel.chosenUser.observe(viewLifecycleOwner,{
            it?.let{
                binding.profileImageView.setImageURI(it.userImage)
                binding.fullUserName.text = it.userFullName
                binding.titleOnUser.text = it.userFullName
                viewModel.chosenUserId = it.userId
                viewModel.chosenUser.value = null
            }
        })

        binding.profileImageView.setOnClickListener{
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setNeutralButton("Show image"){_,_->
                val intent = Intent(requireActivity(), PhotoActivity::class.java)
                intent.putExtra("folder","userProfileImages")
                intent.putExtra("id", viewModel.chosenUserId)
                startActivity(intent)
            }
            dialog.setNegativeButton("Cancel",null)
            dialog.show()
        }


        return binding.root
    }

}