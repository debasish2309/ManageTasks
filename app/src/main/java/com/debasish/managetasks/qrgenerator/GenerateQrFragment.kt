package com.debasish.managetasks.qrgenerator

import android.Manifest
import android.content.Intent
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.debasish.managetasks.R
import com.debasish.managetasks.databinding.FragmentGenerateQrBinding
import com.debasish.managetasks.utility.Constants.RC_PERMISSION
import com.debasish.managetasks.utility.Constants.READ_PERMISSION
import com.debasish.managetasks.utility.Constants.WRITE_PERMISSION
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@AndroidEntryPoint
class GenerateQrFragment : Fragment(R.layout.fragment_generate_qr) {

    var bmp:Bitmap? = null

    private val viewModel : GenerateQrViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGenerateQrBinding.bind(view)

        try
        {

            CoroutineScope(Main).launch {

                val answer = async { viewModel.getOnlyTasks() }
                bmp = encodeAsBitmap(answer.await())

                binding.imageView.setImageBitmap(bmp)
            }

        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        setHasOptionsMenu(true)

    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(str:String): Bitmap? {
        val result: BitMatrix
        var bitmap:Bitmap? = null
        try
        {
            result = MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, 450, 450, null)
            val w = result.getWidth()
            val h = result.getHeight()
            val pixels = IntArray(w * h)
            for (y in 0 until h)
            {
                val offset = y * w
                for (x in 0 until w)
                {
                    pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, 450, 0, 0, w, h)
        }
        catch (iae:Exception) {
            iae.printStackTrace()
            return null
        }
        return bitmap
    }

    //Creaate options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_qr,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            R.id.share -> {
                sendFile(bmp)
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }


    //Sending the file using share option
    private fun sendFile(mBitmap: Bitmap?) {
        val bos = ByteArrayOutputStream()
        mBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("image/*")
        val path = MediaStore.Images.Media.insertImage(
            activity?.getContentResolver(),
            mBitmap,
            "shared_file",
            null)
        val imageUri = Uri.parse(path)
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(createChooser(intent, "Share"))
    }
}