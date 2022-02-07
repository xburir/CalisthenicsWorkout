package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.databinding.SkillTargetItemInRecycleviewerBinding


class TargetInSkillListAdapter(target: ArrayList<String>) : RecyclerView.Adapter<TargetInSkillListAdapter.ViewHolder>() {

    private val targets = target

    class ViewHolder constructor(val binding: SkillTargetItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: String) {
            binding.textView9.text = item
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