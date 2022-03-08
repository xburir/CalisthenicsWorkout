package com.example.calisthenicsworkout

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.ActivityTimerBinding
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory

class TimerActivity : AppCompatActivity() {

    private lateinit var viewModel: TimerViewModel;
    private lateinit var viewModelFactory: TimerViewModelFactory;
    private lateinit var binding: ActivityTimerBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timer)
        val application = requireNotNull(this).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = TimerViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(this,viewModelFactory).get(TimerViewModel::class.java)


        PrefUtil.getTrainingId(this)?.let {
            viewModel.trainingId.value = it
            viewModel.loadExercises(it)
            viewModel.loadTraining(it,this)
            Log.i("Debug","Found "+ it)
        }







    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to cancel this training?")
            .setCancelable(true)
            .setPositiveButton("Yes") {_,_->
                if(viewModel.timerState.value == TimerViewModel.State.Running){
                    viewModel.timer.cancel()
                }
                PrefUtil.setPointsEarned(viewModel.points.value!!,this)
                this.finish()
            }
            .setNegativeButton("No") {_,_->
            }
            .setOnCancelListener {
            }
            .show()
    }
}