package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.databinding.SkillTargetItemInRecycleviewerBinding
import com.example.calisthenicsworkout.generated.callback.OnClickListener

class TargetInSkillListAdapter(val clickListener: ClickListener) : ListAdapter<String,TargetInSkillListAdapter.ViewHolder>(TargetDiffCallBack()) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)

    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }



    class ViewHolder private constructor(val binding: SkillTargetItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: String,clickListener: ClickListener) {
            binding.target = item
            binding.clickListener = clickListener
            binding.executePendingBindings()

        }



        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SkillTargetItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class TargetDiffCallBack: DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    class ClickListener(val clickListener: (item: String)->Unit){
        fun onClick(item: String) = clickListener(item)
    }


}