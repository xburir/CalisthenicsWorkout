package com.example.calisthenicsworkout.fragments

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.PhotoActivity
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.TrainingListAdapter
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
                binding.titleOnUser.text = it.userFullName
                binding.points = it.points
                viewModel.chosenUserId = it.userId
                viewModel.chosenUser.value = null



                viewModel.getChosenUsersTrainings(requireContext())

                val adapter = TrainingListAdapter(TrainingListAdapter.TrainingListener { trainingId ->
                    copyTraining(trainingId)
                })
                val manager = LinearLayoutManager(activity)
                binding.usersTrainingsRecyclerView.layoutManager = manager
                binding.usersTrainingsRecyclerView.adapter = adapter


                viewModel.chosenUsersTrainings.observe(viewLifecycleOwner,{ trainingList->
                    if(trainingList.size>0){
                        binding.textView30.text = "Users trainings: (Click to expand)"

                        binding.textView30.setOnClickListener {

                            if(binding.usersTrainingsRecyclerView.visibility == View.GONE){
                                binding.textView30.text = "Users trainings: (Click to collapse)"
                                binding.usersTrainingsRecyclerView.visibility = View.VISIBLE
                            }else{
                                binding.textView30.text = "Users trainings: (Click to expand)"
                                binding.usersTrainingsRecyclerView.visibility = View.GONE
                            }

                        }
                    }else{
                        binding.textView30.text = "Users trainings: (No trainings)"
                    }
                    adapter.submitList(trainingList)

                })


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

    private fun copyTraining(trainingId:String) {
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", trainingId)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context,"Training ID copied to clipboard", Toast.LENGTH_SHORT).show()

    }

}