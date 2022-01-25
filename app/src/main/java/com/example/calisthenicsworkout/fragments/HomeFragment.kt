package com.example.calisthenicsworkout.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FetchDataDialogBinding
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModel
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var viewModel: FetchDataViewModel
    private lateinit var viewModelFactory: FetchDataViewModelFactory
    private lateinit var dialogg: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_home, container, false)

        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = FetchDataViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(FetchDataViewModel::class.java)

        setHasOptionsMenu(true)

        PrefUtil.getLoadSetting(requireContext())?.let {
             if(it){
                 readOnlineData()
                 PrefUtil.setLoadSettings(false,requireContext())
             }
        }







        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_bar_home,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.toString() == "Read Online Data"){

            readOnlineData()


        }
        return super.onOptionsItemSelected(item)
    }

    private fun readOnlineData() {
        dialogg = Dialog(requireContext())
        val dialogBinding = FetchDataDialogBinding.inflate(LayoutInflater.from(requireContext()))
        dialogg.setContentView(dialogBinding.root)
        dialogg.setCancelable(false)
        dialogg.show()

        viewModel.userInfo.observe(viewLifecycleOwner,{
           if(it){
               dialogBinding.userInfoTextView.text = "Done"
           }else{
               dialogBinding.userInfoTextView.text = "..."
           }
        })

        viewModel.skillsInDb.observe(viewLifecycleOwner,{
            dialogBinding.downloadedSkillsTextView.text = "/"+it.toString()
        })
        viewModel.trainingsInDb.observe(viewLifecycleOwner,{
            dialogBinding.downloadedTrainingsTextView.text = "/"+it.toString()
        })

        viewModel.skills.observe(viewLifecycleOwner,{
            dialogBinding.addedSkillsTextView.text = it.size.toString()
        })

        viewModel.trainings.observe(viewLifecycleOwner,{
            dialogBinding.addedTrainingsTextView.text = it.size.toString()
            if(it.size == viewModel.trainingsInDb.value){
                if(it.isNotEmpty()){
                    dialogg.dismiss()
                }

            }
        })


        viewModel.readFireStoreData()



    }




}