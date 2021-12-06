package com.example.calisthenicsworkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.Skill

class SkillListAdapter: ListAdapter<Skill, SkillListAdapter.ViewHolder>(SkillDiffCallBack()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }



    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        val skillName: TextView = itemView.findViewById(R.id.skill_name)
        val skillDescription: TextView = itemView.findViewById((R.id.skill_description))
        val skillImage: ImageView = itemView.findViewById(R.id.skill_image)

        fun bind(item: Skill) {
            skillName.text = item.skillName
            skillDescription.text = item.skillDescription
            skillImage.setImageResource(
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
                val view = layoutInflater.inflate(R.layout.skill_item_in_recycleviewer, parent, false)
                return ViewHolder(view)
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