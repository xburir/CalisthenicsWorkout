package com.example.calisthenicsworkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.SkillItemInRecycleviewerBinding

class SkillListAdapter: ListAdapter<Skill, SkillListAdapter.ViewHolder>(SkillDiffCallBack()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }



    class ViewHolder private constructor(val binding: SkillItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Skill) {
            binding.skillName.text = item.skillName
            binding.skillDescription.text = item.skillDescription
            binding.skillImage.setImageResource(
                when (item.skillName) {
                    "Dip" -> R.drawable.dip
                    "Handstand" -> R.drawable.handstand
                    "Push up" -> R.drawable.push_up
                    "Muscle up" -> R.drawable.muscle_up
                    "Front lever " -> R.drawable.front_lever
                    "Pull up" -> R.drawable.pull_up
                    else -> R.drawable.ic_launcher_background
                }
            )
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SkillItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class SkillDiffCallBack: DiffUtil.ItemCallback<Skill>(){
        override fun areItemsTheSame(oldItem: Skill, newItem: Skill): Boolean {
            return oldItem.skillId == newItem.skillId
        }

        override fun areContentsTheSame(oldItem: Skill, newItem: Skill): Boolean {
            return oldItem == newItem
        }

    }


}