package com.quixoticdev.imageuploader.interfaces

import okhttp3.Call
import okhttp3.Response
import java.io.IOException

interface HttpCallback {
    fun onResponse(response: Response)
    fun onFailure(call: Call, e: IOException)
}