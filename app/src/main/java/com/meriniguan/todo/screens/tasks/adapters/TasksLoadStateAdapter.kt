package com.meriniguan.todo.screens.tasks.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meriniguan.todo.databinding.FragmentTasksBinding
import com.meriniguan.todo.utils.TryAgainAction

class TasksLoadStateAdapter(
    private val retry: TryAgainAction,
    private val adapterItemCount: Int
) : LoadStateAdapter<TasksLoadStateAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): Holder {
        val binding =
            FragmentTasksBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding, retry)
    }

    override fun onBindViewHolder(holder: Holder, loadState: LoadState) {
        holder.bind(loadState, adapterItemCount)
    }

    class Holder(
        private val binding: FragmentTasksBinding,
        private val tryAgainAction: TryAgainAction
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.loadStateView.tryAgainButton.setOnClickListener {
                tryAgainAction()
            }
        }

        /**
         * This one is to use in TasksFragment.
         */
        fun bind(loadState: CombinedLoadStates, adapterItemCount: Int) {
            binding.apply {
                loadStateView.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                tasksRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                loadStateView.tryAgainButton.isVisible = loadState.source.refresh is LoadState.Error
                loadStateView.messageTextView.isVisible = loadState.source.refresh is LoadState.Error

                // no items view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapterItemCount < 1) {
                    tasksRecyclerView.isVisible = false
                    noItemsTextView.isVisible = true
                } else {
                    noItemsTextView.isVisible = false
                }
            }
        }

        /**
         * This one is to use in onBindViewHolder method.
         */
        fun bind(loadState: LoadState, adapterItemCount: Int) {
            binding.apply {
                loadStateView.progressBar.isVisible = loadState is LoadState.Loading
                tasksRecyclerView.isVisible = loadState is LoadState.NotLoading
                loadStateView.tryAgainButton.isVisible = loadState is LoadState.Error
                loadStateView.messageTextView.isVisible = loadState is LoadState.Error

                // no items view
                if (loadState is LoadState.NotLoading &&
                    loadState.endOfPaginationReached &&
                    adapterItemCount < 1) {
                    tasksRecyclerView.isVisible = false
                    noItemsTextView.isVisible = true
                } else {
                    noItemsTextView.isVisible = false
                }
            }
        }
    }
}