package com.debasish.managetasks.qrgenerator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.debasish.managetasks.room.TasksDao
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.ByteArrayOutputStream


class GenerateQrViewModel @ViewModelInject constructor(
    private val tasksDao: TasksDao
) : ViewModel() {


    suspend fun getOnlyTasks() : String {
        var alltasks = tasksDao.getOnlyTasks().joinToString(separator = ",")
        return alltasks
    }

}