package com.quixoticdev.imageuploader.utility

import android.util.Log
import java.io.File
import java.io.IOException
import com.quixoticdev.imageuploader.interfaces.HttpCallback
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.asRequestBody


class OkHttpProxy() {
    val client = OkHttpClient()
    val UPLOAD_IMAGE_URL = "http://192.168.1.26:1337/UploadImage"

    fun UploadImage(filename: String, sourceImageFile: File, mediaType: MediaType,
                    callback: HttpCallback) {
        val urlBuilder = UPLOAD_IMAGE_URL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", filename, sourceImageFile.asRequestBody(mediaType))
            .build()

        val request =  Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(call, e);
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try{
                        callback.onResponse(response)
                    }
                    catch(e: Exception){
                        Log.i("TAG", "Some exception $e")
                    }
                }
            }
        })
    }


}