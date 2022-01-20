package com.example.calisthenicsworkout.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FetchDataDialogBinding
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModel
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModelFactory

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
        viewModel = ViewModelProvider(this,viewModelFactory).get(FetchDataViewModel::class.java)

        setHasOptionsMenu(true)

        requireActivity().intent.getStringExtra("fetchData")?.let {
            if(it == "yes"){
                readOnlineData()
                requireActivity().intent.putExtra("fetchData","no")
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

        viewModel.userInfo.observe(viewLifecycleOwner,{
           if(it){
               dialogBinding.userInfoTextView.text = "Done"
           }else{
               dialogBinding.userInfoTextView.text = "..."
           }
        })

        viewModel.skillsInDb.observe(viewLifecycleOwner,{
            dialogBinding.downloadedSkillsTextView.text = "/"+it.toString()
            checkFinishedDownload(dialogBinding)
        })
        viewModel.trainingsInDb.observe(viewLifecycleOwner,{
            dialogBinding.downloadedTrainingsTextView.text = "/"+it.toString()
            checkFinishedDownload(dialogBinding)
        })

        viewModel.skills.observe(viewLifecycleOwner,{
            dialogBinding.addedSkillsTextView.text = it.size.toString()
            checkFinishedDownload(dialogBinding)
        })
        viewModel.trainings.observe(viewLifecycleOwner,{
            dialogBinding.addedTrainingsTextView.text = it.size.toString()
            checkFinishedDownload(dialogBinding)
        })


        viewModel.readFireStoreData()

        dialogg.show()

    }

    private fun checkFinishedDownload(dialogBinding: FetchDataDialogBinding) {
       if(dialogBinding.addedTrainingsTextView.text.toString() == viewModel.trainingsInDb.value.toString()){
           if(dialogBinding.addedSkillsTextView.text.toString() == viewModel.skillsInDb.value.toString()){
               if(dialogBinding.userInfoTextView.text.toString() == "Done"){
                   dialogg.dismiss()
               }
           }
       }
    }


}