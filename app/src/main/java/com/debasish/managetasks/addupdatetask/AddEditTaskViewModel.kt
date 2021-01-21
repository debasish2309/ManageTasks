package com.debasish.managetasks.addupdatetask

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.debasish.managetasks.room.Tasks
import com.debasish.managetasks.room.TasksDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditTaskViewModel @ViewModelInject constructor(
    private val tasksDao: TasksDao
) : ViewModel() {

    private val timeStamp: String = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())

    fun addTask(task : String){
        CoroutineScope(IO).launch {
            tasksDao.insertTask(
                Tasks(0,
                task,
                timeStamp,
                false)
            )
        }
    }
}