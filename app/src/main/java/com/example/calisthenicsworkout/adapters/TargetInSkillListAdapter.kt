package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.SkillTargetItemInRecycleviewerBinding
import kotlin.coroutines.coroutineContext


class TargetInSkillListAdapter(target: ArrayList<String>) : RecyclerView.Adapter<TargetInSkillListAdapter.ViewHolder>() {

    private val targets = target

    class ViewHolder constructor(val binding: SkillTargetItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            when(item){
                "abs" ->{
                    binding.targetImageInRecycler.setImageResource(R.drawable.abs)
                }
                "legs" ->{
                    binding.targetImageInRecycler.setImageResource(R.drawable.legs)
                }
                "chest" ->{
                binding.targetImageInRecycler.setImageResource(R.drawable.chest)
                }
                "shoulders" ->{
                    binding.targetImageInRecycler.setImageResource(R.drawable.shoulder)
                }
                "back" ->{
                    binding.targetImageInRecycler.setImageResource(R.drawable.back)
                }
                "arms" ->{
                    binding.targetImageInRecycler.setImageResource(R.drawable.arm)
                }

            }
        }


        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SkillTargetItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }


    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(targets[position])
    }

    override fun getItemCount(): Int {
        return targets.size
    }
}