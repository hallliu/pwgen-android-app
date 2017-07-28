package com.hallliu.passwordgenerator

import android.app.Application
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AndroidModule(private val app: Application) {
    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideClipboardManager(): ClipboardManager {
        return app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    @Provides
    @Singleton
    fun provideSearchManager(): SearchManager {
        return app.getSystemService(Context.SEARCH_SERVICE) as SearchManager
    }
}