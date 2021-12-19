package com.example.calisthenicsworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.calisthenicsworkout.database.SkillDatabase
import com.example.calisthenicsworkout.databinding.ActivityTimerBinding
import com.example.calisthenicsworkout.fragments.ChooseRestFragmentDirections
import com.example.calisthenicsworkout.viewmodels.SkillViewModel
import com.example.calisthenicsworkout.viewmodels.SkillViewModelFactory
import com.example.calisthenicsworkout.viewmodels.TimerViewModel
import com.example.calisthenicsworkout.viewmodels.TimerViewModelFactory
import java.util.*

class TimerActivity : AppCompatActivity() {


    private lateinit var binding: ActivityTimerBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_timer)









    }
}