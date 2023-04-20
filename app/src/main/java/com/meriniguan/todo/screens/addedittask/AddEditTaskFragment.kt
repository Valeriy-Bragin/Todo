package com.meriniguan.todo.screens.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meriniguan.todo.R
import com.meriniguan.todo.databinding.FragmentAddEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private lateinit var binding: FragmentAddEditTaskBinding

    private val viewModel by viewModels<AddEditTaskViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddEditTaskBinding.bind(view)

        fillViews()

        setOnClickListeners()

        observeEvents()
    }

    private fun setOnClickListeners() {
        binding.apply {
            taskNameEditText.addTextChangedListener {
                viewModel.onTaskNameEditTextTextChanged(it.toString())
            }
            importantCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onImportantCheckboxCheckedChange(isChecked)
            }
            saveFab.setOnClickListener { viewModel.onSaveClick() }
        }
    }

    private fun fillViews() {
        binding.apply {
            taskNameEditText.setText(viewModel.name)
            importantCheckbox.isChecked = viewModel.isImportant
            dateCreatedTextView.text = if (viewModel.dateCreatedFormatted.isNotEmpty())
                getString(R.string.created, viewModel.dateCreatedFormatted) else ""
        }
    }

    private fun observeEvents() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                AddEditTaskViewModel.Event.NavigateBack -> {
                    findNavController().popBackStack()
                }
                is AddEditTaskViewModel.Event.SetFragmentResult -> {
                    setFragmentResult(
                        event.requestKey,
                        bundleOf(event.resultKey to event.result)
                    )
                }
            }
        }
    }
}