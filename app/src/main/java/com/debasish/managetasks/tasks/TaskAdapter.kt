package com.debasish.managetasks.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.debasish.managetasks.room.Tasks
import com.debasish.managetasks.databinding.LayoutSingleItemBinding

class TaskAdapter(private val listener: OnItemClickListener) : ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = LayoutSingleItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TaskViewHolder(private val binding: LayoutSingleItemBinding) :RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task =getItem(position)
                        listener.onItemClick(task)
                    }
                }
                checkbox.setOnClickListener {
                    val position = adapterPosition
                    if(position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onCheckBoxClick(task,checkbox.isChecked)
                    }
                }
            }
        }

        fun bind(tasks: Tasks){
            binding.apply {
                checkbox.isChecked = tasks.completed
                tvTask.text = tasks.task
                tvTaskDate.text = tasks.dateCreated

            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(tasks: Tasks)
        fun onCheckBoxClick(tasks: Tasks, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks) =
            oldItem == newItem

    }


}