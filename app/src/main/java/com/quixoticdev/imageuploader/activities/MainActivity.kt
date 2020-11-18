package com.quixoticdev.imageuploader.main

import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import com.quixoticdev.imageuploader.R
import com.quixoticdev.imageuploader.interfaces.HttpCallback
import com.quixoticdev.imageuploader.utility.ApplicationContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response


class MainActivity : AppCompatActivity() {
    private val RESULT_LOAD_IMG: Int = 1
    private  var IMAGE_URI : Uri? = null
    private lateinit var appContext : ApplicationContext
    private lateinit var storageFilePath : String;
    override fun onStart(){
        super.onStart();
        appContext =  (application as ApplicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsCheck()
        setContentView(R.layout.activity_main)
        selectImageBtn.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG)
        }

        uploadImageBtn.setOnClickListener(){
            try {
                val imageToUpload = File(this.storageFilePath);
                var MEDIA_TYPE = imageToUpload.name.toMediaTypeOrNull()
                if (MEDIA_TYPE == null) {
                    MEDIA_TYPE = "image/jpeg".toMediaType()
                }
                appContext.HttpProxy.UploadImage(
                    imageToUpload.name,
                    imageToUpload,
                    MEDIA_TYPE!!,
                    ImageUploadCallback(WeakReference(this))
                )
            }
            catch (e: Exception){

            }
        }
    }

    private fun permissionsCheck() {
        val permissions = arrayOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returningIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returningIntent)
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            RESULT_LOAD_IMG -> {
                this.IMAGE_URI = returningIntent!!.data
                //update method to handle for different Android SDK Versions
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor: Cursor? = contentResolver.query(
                    this.IMAGE_URI!!,
                    filePathColumn,
                    null,
                    null,
                    null
                )
                if (cursor!!.moveToFirst()) {
                    val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                    this.storageFilePath = cursor.getString(columnIndex)
                }
                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            this.IMAGE_URI
                        )
                        imagePreviewView.setImageBitmap(bitmap)
                    } else {
                        val source = ImageDecoder.createSource(
                            this.contentResolver,
                            this.IMAGE_URI!!
                        )
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        imagePreviewView.setImageBitmap(bitmap)
                    }
                } catch (e: IOException) {
                    Log.i("TAG", "Error when selecting image: $e")
                }
            }
        }
    }

    class ImageUploadCallback(activity: WeakReference<Activity>) : HttpCallback {
        val _activity = activity;

        override fun onResponse(response: Response) {
            if(_activity.get() != null){
                val activty =  _activity.get()!!
                activty.runOnUiThread(Runnable {
                    Toast.makeText(activty, "Ur Img Uploaded A.O.K", Toast.LENGTH_LONG).show()
                    val imgPreview = activty.findViewById<ImageView>(R.id.imagePreviewView)
                    imgPreview.setImageResource(android.R.color.transparent)
                })
            }

        }

        override fun onFailure(call: Call, e: IOException) {

        }

    }
}