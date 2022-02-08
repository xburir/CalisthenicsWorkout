package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.Training
import com.example.calisthenicsworkout.databinding.TrainingItemInRecycleviewerBinding

class TrainingListAdapter(val clickListener: TrainingListener): ListAdapter<Training, TrainingListAdapter.ViewHolder>(TrainingListAdapter.TrainingDiffCallBack()) {

    override fun onBindViewHolder(holder: TrainingListAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):TrainingListAdapter.ViewHolder {
        return TrainingListAdapter.ViewHolder.from(parent)
    }
    class ViewHolder private constructor(val binding: TrainingItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Training, clickListener: TrainingListener) {
            binding.training = item
            val context = binding.trainingImageInRecycleview.context
            val layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            val adapter = TargetInSkillListAdapter(TargetInSkillListAdapter.ClickListener{ target ->
                Toast.makeText(context,target, Toast.LENGTH_SHORT).show()
            })
            adapter.submitList(item.target)

            binding.trainingTargetInRecycleview.layoutManager = layoutManager
            binding.trainingTargetInRecycleview.adapter = adapter

            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TrainingItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }
    class TrainingDiffCallBack: DiffUtil.ItemCallback<Training>(){
        override fun areItemsTheSame(oldItem: Training, newItem: Training): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Training, newItem: Training): Boolean {
            return oldItem == newItem
        }

    }
    class TrainingListener(val clickListener: (trainingId: String)->Unit){
        fun onClick(training: Training) = clickListener(training.id)
    }
}