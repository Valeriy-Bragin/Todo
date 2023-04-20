package com.meriniguan.todo.screens.deleteallcompletedtasks

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meriniguan.todo.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeleteAllCompletedTasksFragment : DialogFragment() {

    private val viewModel by viewModels<DeleteAllCompletedTasksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeEvents()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_deletion)
            .setMessage(R.string.delete_confirmation_question)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.onConfirmClick()
            }
            .create()
    }

    private fun observeEvents() = viewModel.applicationScope.launch {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is DeleteAllCompletedTasksViewModel.Event.SetFragmentResult -> {
                    setFragmentResult(
                        event.requestKey,
                        bundleOf(event.resultKey to event.result)
                    )
                }
            }
        }
    }
}