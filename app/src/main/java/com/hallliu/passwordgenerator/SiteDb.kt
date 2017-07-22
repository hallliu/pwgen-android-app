package com.hallliu.passwordgenerator

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import javax.inject.Inject

const val MAIN_TABLE_NAME = "site_table"
const val COLUMN_ID = "_id"
// site name, e.g. www.facebook.com. string
const val COLUMN_SITE_NAME = "site_name"
// length of password. usually 16, but can be shorter. integer.
const val COLUMN_PASSWORD_LENGTH = "length"
// characters to be used in generating the base64 encoding. order dependent. string.
const val COLUMN_PERMITTED_CHARS = "permitted_chars"
// denotes the version of the password. integer.
const val COLUMN_VERSION = "version"
// last modified date (unix time, UTC)
const val COLUMN_LAST_MODIFIED = "last_modified"

// Name for the table used to store requirements for the passwords
const val PATTERNS_TABLE_NAME = "patterns_table"
// Name for the pattern column (stored as strings in Java regex format)
const val COLUMN_PATTERN = "pattern"
// Name for the reference to the site in the main table
const val COLUMN_SITE_ID = "site_id"

class SiteDbHelper @Inject constructor(context: Context) : SQLiteOpenHelper(context,
        SiteDbHelper.Companion.DB_NAME, null, SiteDbHelper.Companion.DB_VERSION) {
    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "site_db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
                |CREATE TABLE $MAIN_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY,
                | $COLUMN_SITE_NAME TEXT UNIQUE,
                | $COLUMN_PASSWORD_LENGTH INTEGER, $COLUMN_PERMITTED_CHARS TEXT,
                | $COLUMN_VERSION INTEGER, $COLUMN_LAST_MODIFIED INTEGER)""".trimMargin())
        db.execSQL("""
                | CREATE TABLE $PATTERNS_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY,
                | $COLUMN_PATTERN TEXT,
                | $COLUMN_SITE_ID INTEGER NOT NULL,
                | FOREIGN KEY($COLUMN_SITE_ID) REFERENCES $MAIN_TABLE_NAME($COLUMN_ID))"""
                .trimMargin())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No upgrades yet
    }
}