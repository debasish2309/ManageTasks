package com.debasish.managetasks.tasks

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.debasish.managetasks.R
import com.debasish.managetasks.room.Tasks

import com.debasish.managetasks.databinding.FragmentTasksBinding
import com.debasish.managetasks.utility.Constants
import com.debasish.managetasks.utility.Constants.CAMERA_PERMISSION
import com.debasish.managetasks.utility.Constants.RC_PERMISSION
import com.debasish.managetasks.utility.Constants.READ_PERMISSION
import com.debasish.managetasks.utility.Constants.WRITE_PERMISSION
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tasks.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) , TaskAdapter.OnItemClickListener {

    private val viewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val taskAdapter = TaskAdapter(this)

        binding.apply {
            recyclerview.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val tasks = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(tasks)
                }
            }).attachToRecyclerView(recyclerview)
            fab.setOnClickListener {
                val action = TasksFragmentDirections.actionTasksFragmentToAddTaskFragment()
                findNavController().navigate(action)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when(event) {
                    is TaskViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(),"Task Deleted",Snackbar.LENGTH_LONG)
                            .setAction("UNDO"){
                                viewModel.onUndoDeleteClick(event.tasks)
                            }.show()
                    }
                }
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.generateqr -> {
                if(checkPermissions()){
                    val action = TasksFragmentDirections.actionTasksFragmentToGenerateQrFragment()
                    findNavController().navigate(action)
                } else{
                    requestPermissions()
                }

                true
            }
            R.id.scan_gallery -> {
                if (checkPermissions()) pickImageForScan() else requestPermissions()
                true
            } R.id.scan_camera -> {
                if(checkPermissions()){
                    val action = TasksFragmentDirections.actionTasksFragmentToScanQrFragment()
                    findNavController().navigate(action)
                } else{
                    requestPermissions()
                }

                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(tasks: Tasks) {
        viewModel.onTaskSelected(tasks)
    }

    override fun onCheckBoxClick(tasks: Tasks, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(tasks, isChecked)
    }

    fun pickImageForScan(){
        var pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        startActivityForResult(pickIntent, 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            111 -> {
                if (data?.data == null)
                { Log.e("TAG", "The uri is null, probably the user cancelled the image selection process using the back button.")
                    return
                }
                var uri = data.data
                try {
                    val inputStream = uri?.let { activity?.getContentResolver()?.openInputStream(it) }
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap == null) {
                        Log.e("TAG", "uri is not a bitmap," + uri.toString())
                        return
                    }
                    val width = bitmap.getWidth()
                    val height = bitmap.getHeight()
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
                    bitmap.recycle()
                    bitmap = null
                    val source = RGBLuminanceSource(width, height, pixels)
                    val bBitmap = BinaryBitmap(HybridBinarizer(source))
                    val reader = MultiFormatReader()
                    try {
                        val result = reader.decode(bBitmap).text.toString()
                        val listItems = result.split(",").toTypedArray()
                        for (str in listItems){
                            viewModel.addTask(str)
                        }
                    } catch (e: NotFoundException) {
                        Log.e("TAG", "decode exception", e)
                    }
                } catch (e: FileNotFoundException) {
                    Log.e("TAG", "can not open file" + uri.toString(), e)
                }
            }
        }


    }


    private fun requestPermissions() {
        activity?.let { ActivityCompat.requestPermissions(it, arrayOf(CAMERA_PERMISSION, WRITE_PERMISSION, READ_PERMISSION), RC_PERMISSION) }
    }

    private fun checkPermissions(): Boolean {
        return ((activity?.let { ActivityCompat.checkSelfPermission(it, CAMERA_PERMISSION) }) == PackageManager.PERMISSION_GRANTED
                && ((activity?.let { ActivityCompat.checkSelfPermission(it, WRITE_PERMISSION ) }) == PackageManager.PERMISSION_GRANTED
                && ((activity?.let { ActivityCompat.checkSelfPermission(it, READ_PERMISSION ) }) == PackageManager.PERMISSION_GRANTED)))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            RC_PERMISSION -> {
                var allPermissionsGranted = false
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false
                        break
                    } else {
                        allPermissionsGranted = true
                    }
                }
                if (allPermissionsGranted) pickImageForScan() else permissionsNotGranted()
            }
        }
    }

    private fun permissionsNotGranted() {
        activity?.let {
            AlertDialog.Builder(it).setTitle("Permissions required")
                .setMessage("These permissions are required to use this app. Please allow Camera and Audio permissions first")
                .setCancelable(false)
                .setPositiveButton("Grant") { dialog, which -> requestPermissions() }
                .show()
        }
    }

}