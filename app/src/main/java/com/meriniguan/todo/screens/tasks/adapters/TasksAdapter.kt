package com.meriniguan.todo.screens.tasks.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meriniguan.todo.databinding.ItemTaskBinding
import com.meriniguan.todo.model.task.entities.Task

class TasksAdapter(private val listener: OnItemClickListener) :
    PagingDataAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position) ?: return
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val task = getCurrentTask() ?: return@setOnClickListener
                    listener.onItemClick(task)
                }
                completedCheckBox.setOnClickListener {
                    val task = getCurrentTask() ?: return@setOnClickListener
                    listener.onCompletedCheckBoxCheckedChange(task, completedCheckBox.isChecked)
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                completedCheckBox.isChecked = task.isCompleted
                taskTextView.text = task.name
                taskTextView.paint.isStrikeThruText = task.isCompleted
                importantImageView.isVisible = task.isImportant
            }
        }

        private fun getCurrentTask(): Task? {
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                return getItem(position)
            }
            return null
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCompletedCheckBoxCheckedChange(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}