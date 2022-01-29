package com.example.calisthenicsworkout.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FetchDataDialogBinding
import com.example.calisthenicsworkout.databinding.FragmentHomeBinding
import com.example.calisthenicsworkout.util.InternetUtil
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModel
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

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
        dialogg.setCancelable(true)
        dialogg.show()

        CoroutineScope(IO).launch {
            if(InternetUtil.checkSpeed()){
                CoroutineScope(Main).launch {

                    viewModel.skillsInDb.observe(viewLifecycleOwner,{
                        dialogBinding.downloadedSkillsTextView.text = "/"+it.toString()
                    })
                    viewModel.trainingsInDb.observe(viewLifecycleOwner,{
                        dialogBinding.downloadedTrainingsTextView.text = "/"+it.toString()
                    })

                    viewModel.skills.observe(viewLifecycleOwner,{
                        if(it.isNotEmpty()){
                            dialogBinding.addedSkillsTextView.text = it.size.toString()
                            dialogBinding.addedSkillsTextView.visibility = View.VISIBLE
                            dialogBinding.textView10.visibility = View.VISIBLE
                            dialogBinding.downloadedSkillsTextView.visibility = View.VISIBLE
                        }
                    })

                    viewModel.trainings.observe(viewLifecycleOwner,{
                        dialogBinding.addedTrainingsTextView.text = it.size.toString()
                        if(it.isNotEmpty()) {
                            dialogBinding.addedTrainingsTextView.visibility = View.VISIBLE
                            dialogBinding.textView11.visibility = View.VISIBLE
                            dialogBinding.downloadedTrainingsTextView.visibility = View.VISIBLE
                            if(it.size == viewModel.trainingsInDb.value){
                                dialogg.dismiss()
                            }
                        }
                    })


                    viewModel.readFireStoreData()
                }
            }else{
                CoroutineScope(Main).launch {
                    Toast.makeText(context,"Internet is too slow to download data, sorry.",Toast.LENGTH_SHORT).show()
                    dialogg.dismiss()
                }
            }
        }






    }




}