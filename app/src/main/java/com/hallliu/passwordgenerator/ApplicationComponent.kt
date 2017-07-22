package com.hallliu.passwordgenerator

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: AddSiteActivity)
    fun getSetMasterPwDialogFragment(): SetMasterPwDialogFragment
    fun getEditIncludedSymbolsDialogFragment(): EditIncludedSymbolsDialogFragment
}