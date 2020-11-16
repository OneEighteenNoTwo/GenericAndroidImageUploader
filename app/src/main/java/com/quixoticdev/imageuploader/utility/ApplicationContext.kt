package com.quixoticdev.imageuploader.utility

import android.app.Application

class ApplicationContext : Application() {

    lateinit var HttpProxy : OkHttpProxy

    override fun onCreate() {
        super.onCreate()
        HttpProxy = OkHttpProxy()
    }
}