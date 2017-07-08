package com.hallliu.passwordgenerator

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(mainActivity: MainActivity)
}