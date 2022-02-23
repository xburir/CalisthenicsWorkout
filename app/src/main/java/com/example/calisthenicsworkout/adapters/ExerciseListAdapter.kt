package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.Exercise
import com.example.calisthenicsworkout.databinding.ExerciseItemInRecycleviewerBinding


class ExerciseListAdapter(val clickListener: ExerciseListener): ListAdapter<Exercise, ExerciseListAdapter.ViewHolder>(ExerciseDiffCallBack()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }



    class ViewHolder private constructor(val binding: ExerciseItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: Exercise, clickListener: ExerciseListener) {
            binding.exercise = item
            binding.clickListener = clickListener




            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ExerciseItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }

    class ExerciseDiffCallBack: DiffUtil.ItemCallback<Exercise>(){
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }

    class ExerciseListener(val clickListener: (skillId: String)->Unit){
        fun onClick(exercise: Exercise) = clickListener(exercise.skillId)
    }


}