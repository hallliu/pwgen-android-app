package com.hallliu.passwordgenerator

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val TABLE_NAME = "site_table"
const val COLUMN_ID = "_id"
// site name, e.g. www.facebook.com. string
const val COLUMN_SITE_NAME = "site_name"
// length of password. usually 16, but can be shorter. integer.
const val COLUMN_PASSWORD_LENGTH = "length"
// characters to be used in generating the base64 encoding. order dependent. string.
const val COLUMN_PERMITTED_CHARS = "permitted_chars"
// characters that must be in the resultant password. order independent. string.
const val COLUMN_REQUIRED_CHARS = "required_chars"
// denotes the version of the password. integer.
const val COLUMN_VERSION = "version"

class SiteDbHelper(val context: Context) : SQLiteOpenHelper(context,
        SiteDbHelper.Companion.DB_NAME, null, SiteDbHelper.Companion.DB_VERSION) {
    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "site_db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("""
                |CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_SITE_NAME TEXT,
                | $COLUMN_PASSWORD_LENGTH INTEGER, $COLUMN_PERMITTED_CHARS TEXT,
                | $COLUMN_REQUIRED_CHARS TEXT, $COLUMN_VERSION INTEGER""".trimMargin())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No upgrades yet
    }
}