package com.hallliu.passwordgenerator

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule(private val activity: Activity) {
    @Provides
    @Singleton
    fun provideActivityContext(): Context = activity

    @Provides
    @Singleton
    fun provideClipboardManager(): ClipboardManager {
        return activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}