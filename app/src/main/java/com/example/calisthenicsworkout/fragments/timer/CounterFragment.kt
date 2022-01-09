package com.example.calisthenicsworkout.fragments.timer

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.TimerExpiredReceiver
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.FragmentCounterBinding
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory
import java.util.*


class CounterFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel;
    private lateinit var viewModelFactory: TimerViewModelFactory;
    private lateinit var binding: FragmentCounterBinding




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_counter, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = SkillDatabase.getInstance(application).skillDatabaseDao()
        viewModelFactory = TimerViewModelFactory(dataSource,application);
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory).get(TimerViewModel::class.java)
        binding.lifecycleOwner = this







        viewModel.allExercisesFinished.observe(viewLifecycleOwner,{
            if(it == true){
                viewModel.timer.cancel()
                requireActivity().finish()
            }
        })

        viewModel.training.observe(viewLifecycleOwner,{
            it?.let{
                binding.trainingNameText.text = it.name
            }
        })

        viewModel.currentExercise.observe(viewLifecycleOwner,{ exercise->
            binding.exerciseNumber.text = "Exercise number "+exercise.order.toString()+"/"+viewModel.exercises.size.toString()

        })

        viewModel.currentSet.observe(viewLifecycleOwner,{ currentSet ->
            binding.setNumber.text = "Set number "+currentSet.toString()+"/" + viewModel.exercises[viewModel.exercisesDone].sets
        })

        viewModel.secondsRemaining.observe(viewLifecycleOwner,{ it?.let { secondsRemaining ->
            if(viewModel.currentSet.value == 0){
                binding.countDownTime.text = "Prepare yourself"
                binding.progressBar.progress = binding.progressBar.max
            }else if(viewModel.timerState.value == TimerViewModel.State.Running){
                val secondsStr = (secondsRemaining+1).toString()
                binding.countDownTime.text = secondsStr
                binding.progressBar.progress = (secondsRemaining+1).toInt()
            }else{
                vibratePhone(500)
                if(viewModel.allExercisesFinished.value == false){
                    val exercise = viewModel.exercises[viewModel.exercisesDone]
                    val reps = exercise.repetitions.split(" ")
                    binding.countDownTime.text = exercise.skillName+"\nx"+reps[0]
                    binding.progressBar.max = viewModel.timerSeconds.value!!.toInt()
                    binding.progressBar.progress =  viewModel.timerSeconds.value!!.toInt()
                }
            }


        }})


        viewModel.timerState.observe(viewLifecycleOwner,{ it?.let{ timerState ->
            when(timerState){
                TimerViewModel.State.Running -> {
                    binding.playPauseButton.text = "Pause"
                    binding.stopTrainingButton.isEnabled = true
                    binding.skipCountDownButton.isEnabled = true
                }
                TimerViewModel.State.Stopped -> {
                    binding.stopTrainingButton.isEnabled = true
                    binding.skipCountDownButton.isEnabled = false
                    if(viewModel.currentSet.value == 0){
                        binding.playPauseButton.text = "Start"
                    }else{
                        binding.playPauseButton.text = "Finished set"
                    }
                }
                TimerViewModel.State.Paused -> {
                    binding.playPauseButton.text = "Resume"
                    binding.skipCountDownButton.isEnabled = true
                    binding.stopTrainingButton.isEnabled = true
                }
            }
        }
        })

        viewModel.timerSeconds.observe(viewLifecycleOwner, { it?.let{ timerSeconds ->
            binding.progressBar.max = timerSeconds.toInt()
        } })



        binding.playPauseButton.setOnClickListener{
            viewModel.playPauseClick()
        }

        binding.skipCountDownButton.setOnClickListener{
            viewModel.skipClicked()
        }

        binding.stopTrainingButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setMessage("Are you sure you want to cancel this training?")
                .setCancelable(true)
                .setPositiveButton("Yes") {_,_->
                    if (viewModel.timerState.value == TimerViewModel.State.Running){
                        viewModel.timer.cancel()
                    }
                    requireActivity().finish()
                }
                .setNegativeButton("No") {_,_->
                }
                .setOnCancelListener {
                }
                .show()
        }



        return binding.root
    }

    fun vibratePhone(time: Long) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =  requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
             val vib  = vibratorManager.defaultVibrator;
             vib.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
             val vib = requireActivity().getSystemService(VIBRATOR_SERVICE) as Vibrator
             vib.vibrate(time)
        }
    }

}