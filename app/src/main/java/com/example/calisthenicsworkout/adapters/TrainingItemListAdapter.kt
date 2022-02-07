package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.TrainingItem
import com.example.calisthenicsworkout.databinding.CounterTrainingItemInRecycleviewerBinding

class TrainingItemListAdapter() : ListAdapter<TrainingItem, TrainingItemListAdapter.ViewHolder>(CounterTrainingItemDiffCallBack()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: CounterTrainingItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: TrainingItem) {
            binding.item = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CounterTrainingItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class CounterTrainingItemDiffCallBack: DiffUtil.ItemCallback<TrainingItem>(){
        override fun areItemsTheSame(oldItem: TrainingItem, newItem: TrainingItem): Boolean {
            return (oldItem.name == newItem.name)
        }

        override fun areContentsTheSame(oldItem: TrainingItem, newItem: TrainingItem): Boolean {
            return oldItem.name == newItem.name
        }
    }
}