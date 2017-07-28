package com.hallliu.passwordgenerator

import android.app.Application

class PasswordGeneratorApp : Application() {
    lateinit var depGraph: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        depGraph = DaggerApplicationComponent.builder().androidModule(AndroidModule(this)).build()
    }
}