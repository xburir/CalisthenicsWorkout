package com.example.calisthenicsworkout.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.AuthActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentProfileBinding
import com.example.calisthenicsworkout.databinding.FragmentSkillBinding
import com.example.calisthenicsworkout.viewmodels.AuthViewModel
import com.example.calisthenicsworkout.viewmodels.AuthViewModelFactory
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var viewModel: AuthViewModel;
    private lateinit var viewModelFactory: AuthViewModelFactory;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentProfileBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_profile,container,false)


        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = AuthViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(AuthViewModel::class.java)
        binding.lifecycleOwner = this

        viewModel.database.getUser(FirebaseAuth.getInstance().currentUser!!.uid).observe(viewLifecycleOwner,{
            Log.i("Debug","observing")
            it?.let { user ->
                Log.i("Debug","found")
                binding.fullUserName.text = user.userFullName
                binding.userEmail.text = user.userEmail
                binding.userId.text = user.userId
            }
        })

        binding.logoutButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                withContext(Dispatchers.IO){
                    viewModel.database.clearExerciseTable()
                    viewModel.database.clearSkillAndSkillsCrossRefTable()
                    viewModel.database.clearUserTable()
                    viewModel.database.clearSkillsTable()
                    viewModel.database.clearTrainingTable()
                    viewModel.database.clearUserAndSkillsTable()

                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(context, AuthActivity::class.java))
                    requireActivity().finish()
                }
            }

        }


        return binding.root
    }

}