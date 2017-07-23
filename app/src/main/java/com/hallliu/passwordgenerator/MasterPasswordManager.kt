package com.hallliu.passwordgenerator

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

private const val SHARED_PREFS_NAME = "prefs"
private const val MASTER_PW_HASH_KEY = "masterPwHash"

@Singleton
class MasterPasswordManager @Inject constructor(val context: Context) {
    private val prefs: SharedPreferences =
            context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    private var masterPasswordHash: String? = prefs.getString(MASTER_PW_HASH_KEY, null)

    private val masterPwHashChangedListener = SharedPreferences.OnSharedPreferenceChangeListener {
        sharedPreferences, key ->
        if (key == MASTER_PW_HASH_KEY) {
            masterPasswordHash = sharedPreferences.getString(MASTER_PW_HASH_KEY, null)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(masterPwHashChangedListener)
    }

    fun setMasterPwHash(hash: String) {
        masterPasswordHash = hash
        prefs.edit().putString(MASTER_PW_HASH_KEY, hash).apply()
    }

    fun getMasterPwHash(): String? {
        return masterPasswordHash
    }
}