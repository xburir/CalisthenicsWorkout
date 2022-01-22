package com.example.calisthenicsworkout.fragments.timer

import android.app.AlertDialog
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.adapters.SkillListAdapter
import com.example.calisthenicsworkout.adapters.TrainingItemListAdapter
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.database.entities.TrainingItem
import com.example.calisthenicsworkout.databinding.FragmentCounterBinding
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory


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

        val adapter = TrainingItemListAdapter()
        val manager = LinearLayoutManager(activity)
        binding.remainingItemsRecyclerView.layoutManager = manager
        binding.remainingItemsRecyclerView.adapter = adapter










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

                binding.exerciseNumber.text = "Exercise "+(viewModel.exerciseNumber+1) +"/"+viewModel.exercises.size
                if(viewModel._training.type == "circular"){
                    binding.setNumber.text = "Set "+(viewModel.setNumber+1)+"/" + viewModel._training.numberOfSets
                }else{
                    binding.setNumber.text = "Set "+(viewModel.setNumber+1)+"/" + viewModel.exercises[viewModel.exerciseNumber].sets
                }


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

                        if(viewModel.nextItem == viewModel.trainingItems.first()){
                            binding.playPauseButton.text = "Start"
                        }else if(viewModel.exerciseTimer){
                            binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.RED)
                            binding.progressBar.progress =  viewModel.timerSeconds.value!!.toInt()
                            binding.playPauseButton.text = "Start timer"
                            vibratePhone(500)
                        }else if(!viewModel.exerciseTimer){
                            binding.progressBar.supportProgressTintList = ColorStateList.valueOf(Color.GREEN)
                            binding.progressBar.progress =  0
                            binding.playPauseButton.text = "Finished set"
                            vibratePhone(500)
                        }

                        binding.countDownTime.text = viewModel.nextItem.name+"\n"+viewModel.nextItem.reps.toString()+" "+viewModel.nextItem.type

                        viewUpcomingExercises(adapter)





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

    private fun viewUpcomingExercises(adapter: TrainingItemListAdapter) {
        val list = mutableListOf<TrainingItem>()
        for(i in viewModel.item.nextIndex() until viewModel.trainingItems.size){
            list.add(viewModel.trainingItems[i])
        }
        adapter.submitList(list)
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