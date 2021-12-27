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



    companion object{
        fun setAlarm(context: Context, nowMilliSeconds: Long, secondsRemaining: Long): Long{
            val wakeUpTime = nowMilliSeconds+(secondsRemaining*1000)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowMilliSeconds,context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)

        }

        val nowMilliSeconds: Long
            get() = Calendar.getInstance().timeInMillis
    }

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
                Log.i("Debug","finishing")
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
            }else if(viewModel.timerState.value == TimerViewModel.State.Running){
                val secondsStr = (secondsRemaining+1).toString()
                binding.countDownTime.text = secondsStr
            }else{
                vibratePhone(1000)
                playSound()
                binding.progressBar.progress = 0
                if(viewModel.allExercisesFinished.value == false){
                    binding.countDownTime.text = viewModel.exercises[viewModel.exercisesDone].skillName
                }
            }
            binding.progressBar.progress = (secondsRemaining+1).toInt()

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
                    removeAlarm(requireContext())
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

    override fun onResume() {
        super.onResume()
//        viewModel.initTimer()
//        removeAlarm(requireContext())
//        if(viewModel.currentSet.value != 0 && viewModel.timerState.value == TimerViewModel.State.Stopped){
//            binding.countDownTime.text = viewModel.exercises[viewModel.exercisesDone].skillName
//        }
//        NotificationUtil.hideTimerNotification(requireContext())
    }

    override fun onPause() {
        super.onPause()
//        if(viewModel.timerState.value == TimerViewModel.State.Running){
////            timer.cancel()
//            val wakeUpTime = setAlarm(requireContext(), nowMilliSeconds,secondsRemaining)
//            NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
//        }else if (viewModel.timerState.value == TimerViewModel.State.Paused){
//            val wakeUpTime = setAlarm(requireContext(), nowMilliSeconds,secondsRemaining)
//            NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
//        }
//        PrefUtil.setPreviousTimerLengthSeconds(timerSeconds,requireContext())
//        PrefUtil.setSecondsRemaining(secondsRemaining,requireContext())
//        PrefUtil.setTimerState(timerState,requireContext())
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

    fun playSound(){
        val mp = MediaPlayer.create(requireContext(),R.raw.bell)
        mp.start()

    }
}