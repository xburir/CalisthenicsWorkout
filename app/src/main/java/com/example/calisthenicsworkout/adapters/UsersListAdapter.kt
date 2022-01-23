package com.example.calisthenicsworkout.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.calisthenicsworkout.database.entities.User
import com.example.calisthenicsworkout.databinding.UserItemInRecycleviewerBinding

class UsersListAdapter (val clickListener: UserListener): ListAdapter<User, UsersListAdapter.ViewHolder>(UsersListAdapter.UserDiffCallBack()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!,clickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        return ViewHolder.from(parent)
    }
    class ViewHolder private constructor(val binding: UserItemInRecycleviewerBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: User, clickListener: UserListener) {
            binding.user = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = UserItemInRecycleviewerBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
    }
    class UserDiffCallBack: DiffUtil.ItemCallback<User>(){
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

    }
    class UserListener(val clickListener: (userId: String)->Unit){
        fun onClick(user:User) = clickListener(user.userId)
    }
}