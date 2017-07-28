package com.hallliu.passwordgenerator

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface ApplicationComponent {
    fun inject(app: PasswordGeneratorApp)
    fun inject(activity: MainActivity)
    fun inject(activity: AddSiteActivity)
    fun inject(pwgenFragment: PasswordGenerationFragment)
    fun getSetMasterPwDialogFragment(): SetMasterPwDialogFragment
    fun getEditIncludedSymbolsDialogFragment(): EditIncludedSymbolsDialogFragment
}