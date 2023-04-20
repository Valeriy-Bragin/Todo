package com.meriniguan.todo.screens.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.meriniguan.todo.R
import com.meriniguan.todo.databinding.FragmentTasksBinding
import com.meriniguan.todo.screens.addedittask.ADD_EDIT_TASK_REQUEST_KEY
import com.meriniguan.todo.screens.deleteallcompletedtasks.DELETE_ALL_COMPLETED_TASKS_REQUEST_KEY
import com.meriniguan.todo.screens.tasks.adapters.FooterHeaderLoadStateAdapter
import com.meriniguan.todo.screens.tasks.adapters.TasksAdapter
import com.meriniguan.todo.screens.tasks.adapters.TasksLoadStateAdapter
import com.meriniguan.todo.utils.TryAgainAction
import com.meriniguan.todo.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private lateinit var binding: FragmentTasksBinding

    private val viewModel by viewModels<TasksViewModel>()

    private lateinit var tasksAdapter: TasksAdapter

    private var tasksLoadStateHolder: TasksLoadStateAdapter.Holder? = null

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTasksBinding.bind(view)
        tasksAdapter = TasksAdapter(viewModel)
        setupTasksList()
        setUpMenu()

        binding.addFab.setOnClickListener { viewModel.onAddClick() }

        setFragmentResultListeners()

        observeEvents()
    }

    private fun setupTasksList() {
        val tryAgainAction: TryAgainAction = { tasksAdapter.retry() }

        val footerAdapter = FooterHeaderLoadStateAdapter(tryAgainAction)
        val headerAdapter = FooterHeaderLoadStateAdapter(tryAgainAction)

        val adapterWithLoadState =
            tasksAdapter.withLoadStateHeaderAndFooter(headerAdapter, footerAdapter)

        binding.tasksRecyclerView.apply {
            adapter = adapterWithLoadState
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)

            setupSwipingFunc(this)
        }

        tasksLoadStateHolder = TasksLoadStateAdapter.Holder(
            binding,
            tryAgainAction
        )

        observeTasks()
        observeLoadState()
    }

    private fun setUpMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_tasks, menu)

                val searchItem = menu.findItem(R.id.action_search)
                searchView = searchItem.actionView as SearchView

                restoreSearchViewState(searchItem)

                searchView.onQueryTextChanged {
                    viewModel.onSearchQueryChanged(it)
                }

                setHideCompleted(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort_by_name -> {
                        viewModel.onSortTasksByNameSelected()
                        true
                    }
                    R.id.action_sort_by_date_created -> {
                        viewModel.onSortTasksByDateCreatedSelected()
                        true
                    }
                    R.id.action_hide_completed_tasks -> {
                        menuItem.isChecked = !menuItem.isChecked
                        viewModel.onHideCompletedTasksClick(menuItem.isChecked)
                        true
                    }
                    R.id.action_delete_all_completed_tasks -> {
                        viewModel.onDeleteAllCompletedTasksClick()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(ADD_EDIT_TASK_REQUEST_KEY) { _, bundle ->
            viewModel.onAddEditTaskResult(bundle)
        }
        setFragmentResultListener(DELETE_ALL_COMPLETED_TASKS_REQUEST_KEY) { _, bundle ->
            viewModel.onDeleteAllCompletedTasksResult(bundle)
        }
    }

    private fun observeEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                TasksViewModel.Event.InvalidateTasksList -> {
                    tasksAdapter.refresh()
                }
                is TasksViewModel.Event.ShowUndoTaskDeletionMessage -> {
                    Snackbar.make(requireView(), event.messageRes, Snackbar.LENGTH_LONG)
                        .setAction(event.undoButtonTextRes) {
                            viewModel.onUndoTaskDeletionClick(event.task)
                        }
                        .show()
                }
                is TasksViewModel.Event.NavigateToAddEditTaskScreen -> {
                    val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                        getString(event.titleRes), event.task
                    )
                    findNavController().navigate(action)
                }
                TasksViewModel.Event.ShowConfirmDeleteAllCompletedTasksScreen -> {
                    val action = TasksFragmentDirections
                        .actionGlobalDeleteAllCompletedTasksFragment()
                    findNavController().navigate(action)
                }
                is TasksViewModel.Event.ShowMessage -> {
                    Snackbar.make(requireView(), event.messageRes, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupSwipingFunc(recyclerView: RecyclerView) {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val task = tasksAdapter.peek(viewHolder.bindingAdapterPosition) ?: return
                //lastlySwipedItemViewHolder = viewHolder
                viewModel.onTaskSwiped(task)
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun observeTasks() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.tasks.collectLatest {
            tasksAdapter.submitData(it)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeLoadState() = viewLifecycleOwner.lifecycleScope.launch {
        tasksAdapter.loadStateFlow.debounce(200).collectLatest { state ->
            if (tasksLoadStateHolder != null) {
                tasksLoadStateHolder!!.bind(state, tasksAdapter.itemCount)
            }
        }
    }

    private fun restoreSearchViewState(searchItem: MenuItem) {
        val pendingQuery = viewModel.getSearchQuery()
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }
    }

    private fun setHideCompleted(menu: Menu) = viewLifecycleOwner.lifecycleScope.launch {
        menu.findItem(R.id.action_hide_completed_tasks).isChecked =
            viewModel.preferencesFlow.first().hideCompleted
    }
}