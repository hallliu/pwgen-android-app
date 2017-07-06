package com.hallliu.passwordgenerator

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class AndroidModule(private val activity: MainActivity) {
    @Provides
    fun provideActivityContext(): Context = activity
}