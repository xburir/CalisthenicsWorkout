package com.example.calisthenicsworkout.fragments.timer

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
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










        viewModel.training.observe(viewLifecycleOwner,{
            it?.let{
                binding.trainingNameText.text = it.name
            }
        })

        viewModel.secondsRemaining.observe(viewLifecycleOwner,{ it?.let { secondsRemaining ->
                val secondsStr = (secondsRemaining+1).toString()
                binding.countDownTime.text = secondsStr
                binding.progressBar.progress = (secondsRemaining+1).toInt()


        }})


        viewModel.timerState.observe(viewLifecycleOwner,{ it?.let{ timerState ->
            if(!viewModel.allExercisesFinished){
                Log.i("Debug","state observing "+timerState)
                binding.exerciseNumber.text = "Exercise number "+(viewModel.exerciseNumber+1) +"/"+viewModel.exercises.size
                binding.setNumber.text = "Set number "+(viewModel.setNumber+1)+"/" + viewModel.exercises[viewModel.exerciseNumber].sets

                when(timerState){
                    TimerViewModel.State.Running -> {
                        binding.playPauseButton.text = "Pause"
                        if (viewModel.exerciseTimer) {
                            binding.skipCountDownButton.isEnabled = false
                            binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.RED)
                        } else {
                            binding.skipCountDownButton.isEnabled = true
                            binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.GREEN)
                        }
                    }
                    TimerViewModel.State.Stopped -> {
                        binding.skipCountDownButton.isEnabled = false
                        if(viewModel.prepareTimer){
                            binding.countDownTime.text = "Prepare yourself"
                            binding.progressBar.progress = 0
                            binding.playPauseButton.text = "Start"
                        }else {


                            val exercise = viewModel.exercises[viewModel.exerciseNumber]
                            val reps = exercise.repetitions.split(" ")

                            if(viewModel.exerciseTimer){
                                binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.RED)
                                binding.countDownTime.text = exercise.skillName+"\n"+reps[0]+"s"
                                binding.progressBar.progress =  viewModel.timerSeconds.value!!.toInt()
                                binding.playPauseButton.text = "Start timer"
                                vibratePhone(500)
                            }
                            if(!viewModel.exerciseTimer){
                                binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.GREEN)
                                binding.countDownTime.text = exercise.skillName+"\n"+reps[0]+"x"
                                binding.progressBar.progress =  0
                                binding.playPauseButton.text = "Finished set"
                                vibratePhone(500)
                            }



                        }
                    }


                    TimerViewModel.State.Paused -> {
                        binding.playPauseButton.text = "Resume"
                        binding.skipCountDownButton.isEnabled = false
                    }
                }
            }else{
                viewModel.timer.cancel()
                requireActivity().finish()
            }
        } })

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