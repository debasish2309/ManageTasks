package com.debasish.managetasks.addupdatetask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.debasish.managetasks.R
import com.debasish.managetasks.databinding.FragmentAddTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTaskFragment : Fragment(R.layout.fragment_add_task)  {

    private val viewModel : AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddTaskBinding.bind(view)

        binding.buttonSubmit.setOnClickListener {
            viewModel.addTask(binding.insertText.text.toString())

            val action = AddTaskFragmentDirections.actionAddTaskFragmentToTasksFragment()
            findNavController().navigate(action)

        }
    }

}