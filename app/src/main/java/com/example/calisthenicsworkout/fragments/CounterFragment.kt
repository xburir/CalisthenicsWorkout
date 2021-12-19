package com.example.calisthenicsworkout.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.example.calisthenicsworkout.util.NotificationUtil
import com.example.calisthenicsworkout.util.PrefUtil
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory
import java.util.*

class CounterFragment : Fragment() {

    private lateinit var viewModel: TimerViewModel;
    private lateinit var viewModelFactory: TimerViewModelFactory;
    enum class State{
        Stopped,Paused,Running
    }
    private lateinit var timer: CountDownTimer
    private var timerSeconds = 0L
    private var timerState = State.Stopped
    private var secondsRemaining = 0L
    private lateinit var binding: FragmentCounterBinding


    companion object{
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
            val wakeUpTime = (nowSeconds+secondsRemaining)*1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)

        }

        val nowSeconds: Long
            get() = Calendar.getInstance().timeInMillis/1000
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
        PrefUtil.getTrainingId(requireContext())?.let {
            viewModel.trainingId.value = it
            Log.i("Debug","Found "+ it)
        }



        val args = CounterFragmentArgs.fromBundle(requireArguments())
        val timeBetweenExercises = args.betweenExercises.toString()
        val timeBetweenSets = args.betweenSets.toString()

        viewModel.trainingId.observe(viewLifecycleOwner,{
           it?.let {
               viewModel.loadExercises(it,requireActivity())
               viewModel.loadTraining(it,requireActivity())
           }
        })

        viewModel.training.observe(viewLifecycleOwner,{
            it?.let{
                binding.trainingNameText.text = it.name
            }
        })


        viewModel.currentExercise.observe(viewLifecycleOwner,{ exercise->
            binding.exerciseNumber.text = "Exercise number "+exercise.order.toString()+"/"+viewModel.exercises.size.toString()

            viewModel.currentSet.observe(viewLifecycleOwner,{
                binding.setNumber.text = "Set number "+it.toString()+"/"+ exercise.sets
            })

        })




        binding.playPauseButton.setOnClickListener{
            when (timerState){
                State.Running -> {
                    timer.cancel()
                    timerState = State.Paused
                    updateButtons()
                }
                State.Stopped -> {
                    startTimer()
                    timerState = State.Running
                    updateButtons()
                }
                State.Paused -> {
                    startTimer()
                    timerState = State.Running
                    updateButtons()
                }
            }

        }

        binding.skipCountDownButton.setOnClickListener{
            onTimerFinished()
            timer.cancel()
            timerState = State.Stopped
            updateButtons()
        }

        binding.stopTrainingButton.setOnClickListener {

            AlertDialog.Builder(context)
                .setMessage("Are you sure you want to cancel this training?")
                .setCancelable(true)
                .setPositiveButton("Yes") {_,_->
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
        initTimer()
        removeAlarm(requireContext())
        NotificationUtil.hideTimerNotification(requireContext())
    }

    override fun onPause() {
        super.onPause()
        if(timerState == State.Running){
            timer.cancel()
            val wakeUpTime = setAlarm(requireContext(), nowSeconds,secondsRemaining)
            NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
        }else if (timerState == State.Paused){
            val wakeUpTime = setAlarm(requireContext(), nowSeconds,secondsRemaining)
            NotificationUtil.showTimerRunning(requireContext(), wakeUpTime)
        }
        PrefUtil.setPreviousTimerLengthSeconds(timerSeconds,requireContext())
        PrefUtil.setSecondsRemaining(secondsRemaining,requireContext())
        PrefUtil.setTimerState(timerState,requireContext())
    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(requireContext())
        if(timerState == State.Stopped){
            setNewTimerLength()
        }else{
            setPreviousTimerLength()
        }

        when (timerState) {
            State.Paused -> {
                secondsRemaining = PrefUtil.getSecondsRemaining(requireContext())
            }
            State.Running -> {
                secondsRemaining = PrefUtil.getSecondsRemaining(requireContext())
                startTimer()
            }
            else -> {
                secondsRemaining = timerSeconds
            }
        }

        val alarmSetTime = PrefUtil.getAlarmSetTime(requireContext())
        if(alarmSetTime > 0){
            secondsRemaining -= nowSeconds - alarmSetTime
        }
        if(secondsRemaining <= 0){
            onTimerFinished()
        }

        updateCountDownUI()
        updateButtons()
    }

    private fun onTimerFinished(){
        timerState = State.Stopped
        setNewTimerLength()
        binding.progressBar.progress = 0
        PrefUtil.setSecondsRemaining(timerSeconds,requireContext())
        secondsRemaining = timerSeconds

        Log.i("Debug","Finished timer")
        binding.playPauseButton.text = "Start set"
        viewModel.currentSet.value = viewModel.currentSet.value?.plus(1)

        updateCountDownUI()
        updateButtons()
    }

    private fun startTimer(){
        timerState = State.Running
        timer = object : CountDownTimer(secondsRemaining*1000,1000){
            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUI()
            }
        }.start()
    }

    private fun setNewTimerLength(){

        timerSeconds = 5
        binding.progressBar.max = timerSeconds.toInt()
    }

    private fun setPreviousTimerLength(){
        timerSeconds = PrefUtil.getPreviousTimerLengthSeconds(requireContext())
        binding.progressBar.max = timerSeconds.toInt()
    }

    private fun updateCountDownUI(){
        val secondsUntilFinished = secondsRemaining
        val secondsStr = secondsUntilFinished.toString()
        binding.countDownTime.text = secondsStr
        binding.progressBar.progress = secondsRemaining.toInt()
    }

    private fun updateButtons(){
        when(timerState){
            State.Running -> {
                binding.playPauseButton.text = "Pause"
                binding.stopTrainingButton.isEnabled = true
                binding.skipCountDownButton.isEnabled = true
            }
            State.Stopped -> {
                binding.stopTrainingButton.isEnabled = true
                binding.skipCountDownButton.isEnabled = false
            }
            State.Paused -> {
                binding.playPauseButton.text = "Resume"
                binding.skipCountDownButton.isEnabled = true
                binding.stopTrainingButton.isEnabled = true
            }
        }
    }
}