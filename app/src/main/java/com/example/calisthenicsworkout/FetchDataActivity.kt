package com.example.calisthenicsworkout

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.ActivityFetchDataBinding
import com.example.calisthenicsworkout.databinding.FetchDataDialogBinding
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModel
import com.example.calisthenicsworkout.viewmodels.FetchDataViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FetchDataActivity : AppCompatActivity() {

    private lateinit var viewModel: FetchDataViewModel
    private lateinit var viewModelFactory: FetchDataViewModelFactory
    private lateinit var dialogg: Dialog
    private lateinit var binding: ActivityFetchDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fetch_data)
        binding.lifecycleOwner = this


        val application = requireNotNull(this).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = FetchDataViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(FetchDataViewModel::class.java)

        readOnlineData()

    }

    private fun readOnlineData() {

        dialogg = Dialog(this)
        val dialogBinding = FetchDataDialogBinding.inflate(LayoutInflater.from(this))
        dialogg.setContentView(dialogBinding.root)
        dialogg.setCancelable(false)
        dialogg.show()

        viewModel.skillsInDb.observe(this,{
            dialogBinding.downloadedSkillsTextView.text = "/"+it.toString()
        })
        viewModel.trainingsInDb.observe(this,{
            dialogBinding.downloadedTrainingsTextView.text = "/"+it.toString()
        })
        viewModel.customTrainingsInDb.observe(this,{
            dialogBinding.downloadedCustomTrainingsTextView.text = "/"+it.toString()
        })
        viewModel.userAndSkillCrossRefsInDb.observe(this,{
            dialogBinding.downloadedUserSkillsTextView.text = "/"+it.toString()
        })
        viewModel.skillAndSkillCrossRefsInDb.observe(this,{
            dialogBinding.downloadedBeforeSkillsTextView.text = "/"+it.toString()
        })
        viewModel.exercisesInDb.observe(this,{
            dialogBinding.downloadedExercisesTextView.text = "/"+it.toString()
        })

        viewModel.skillsAdded.observe(this,{
            dialogBinding.addedSkillsTextView.text = it.toString()
        })

        viewModel.trainingsAdded.observe(this,{
            dialogBinding.addedTrainingsTextView.text = it.toString()
        })

        viewModel.customTrainingsAdded.observe(this,{
            dialogBinding.addedCustomTrainingsTextView.text = it.toString()
        })
        viewModel.exercisesAdded.observe(this,{
            dialogBinding.addedExercisesTextView.text = it.toString()
        })
        viewModel.beforeSkillsAdded.observe(this,{
            dialogBinding.addedBeforeSkillsTextView.text = it.toString()
        })
        viewModel.userSkillsAdded.observe(this,{
            dialogBinding.addedUserSkillTextView.text = it.toString()
        })

        viewModel.timeLeft.observe(this,{
            dialogBinding.timeOutTextView.text = "Cancelling in ${it.toString()}s."
        })

        viewModel.finished.observe(this,{
            if(it == "done"){
                dialogg.dismiss()
                Toast.makeText(this,"Data downloaded succesfully", Toast.LENGTH_SHORT).show()
            }
            if(it == "downloaded"){
                dialogBinding.downloadedInfoTextView.visibility = View.VISIBLE
                dialogBinding.timeOutTextView.visibility = View.GONE
                Log.i("Debug","downloaded")
            }
            if(it == "under"){
                dialogg.dismiss()
                Toast.makeText(this,"The download request didn't finish under ${viewModel.TIMEOUT/1000}s, " +
                        "please consider downloading the data with better connection", Toast.LENGTH_LONG).show()
            }
        })

        dialogg.setOnDismissListener {
            viewModel.timer.cancel()
            this.finish()
        }

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.readFireStoreData()
        }


    }
}