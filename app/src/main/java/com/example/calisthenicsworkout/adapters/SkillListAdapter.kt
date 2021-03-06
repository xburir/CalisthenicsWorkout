package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.Skill
import com.example.calisthenicsworkout.databinding.SkillItemInRecycleviewerBinding

class SkillListAdapter(val clickListener: SkillListener): ListAdapter<Skill, SkillListAdapter.ViewHolder>(SkillDiffCallBack()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }



    class ViewHolder private constructor(val binding: SkillItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Skill, clickListener: SkillListener) {
            binding.skill = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
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

    class SkillListener(val clickListener: (skillId: String)->Unit){
        fun onClick(skill: Skill) = clickListener(skill.skillId)
    }


}