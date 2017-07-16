package com.hallliu.passwordgenerator

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule(private val activity: Activity) {
    @Provides
    @Singleton
    fun provideActivityContext(): Context = activity
}