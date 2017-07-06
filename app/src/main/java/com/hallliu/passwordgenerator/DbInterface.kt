package com.hallliu.passwordgenerator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Handler
import android.os.HandlerThread

class DbInterface() {
    val handler: Handler
    private val handlerThread = HandlerThread(this.javaClass.simpleName)

    lateinit var db: SQLiteDatabase

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    companion object {
        enum class DbUpdateResult {
            SUCCESS, ALREADY_EXISTS, OTHER_ERROR
        }
    }

    fun getPwSpecForSite(site: String, callback: (result: PasswordSpecification?) -> Unit) {
        handler.post {
            val columns = arrayOf(COLUMN_SITE_NAME, COLUMN_PERMITTED_CHARS,
                    COLUMN_REQUIRED_CHARS, COLUMN_PASSWORD_LENGTH, COLUMN_VERSION)
            val whereClause = COLUMN_SITE_NAME + " = ?"
            val whereArgs = arrayOf(site)
            val cursor = db.query(TABLE_NAME, columns, whereClause, whereArgs, null, null, null)
            if (cursor.count == 0) {
                callback(null)
                cursor.close()
                return@post
            }
            cursor.moveToFirst()
            fun Cursor.getStringByName(name: String): String =
                    this.getString(this.getColumnIndex(name))
            fun Cursor.getIntByName(name: String): Int =
                    this.getInt(this.getColumnIndex(name))
            callback(PasswordSpecification(site,
                    permittedChars = cursor.getStringByName(COLUMN_PERMITTED_CHARS),
                    requiredChars = cursor.getStringByName(COLUMN_REQUIRED_CHARS),
                    pwLength = cursor.getIntByName(COLUMN_PASSWORD_LENGTH),
                    pwVersion = cursor.getIntByName(COLUMN_VERSION)))
            cursor.close()
        }
    }

    fun saveSiteInDb(pwSpec: PasswordSpecification, callback: (DbUpdateResult) -> Unit) {
        handler.post {
            // Check for existing site first
            val cursor = db.query(TABLE_NAME,
                    arrayOf(COLUMN_SITE_NAME),
                    COLUMN_SITE_NAME + " = ?",
                    arrayOf(pwSpec.siteName),
                    null, null, null)
            if (cursor.count > 0) {
                callback(DbUpdateResult.ALREADY_EXISTS)
                cursor.close()
                return@post
            }
            val values = ContentValues()
            values.put(COLUMN_SITE_NAME, pwSpec.siteName)
            values.put(COLUMN_PERMITTED_CHARS, pwSpec.permittedChars)
            values.put(COLUMN_REQUIRED_CHARS, pwSpec.requiredChars)
            values.put(COLUMN_PASSWORD_LENGTH, pwSpec.pwLength)
            values.put(COLUMN_VERSION, pwSpec.pwVersion)
            val row = db.insert(TABLE_NAME, null, values)
            callback(if (row == -1L) DbUpdateResult.OTHER_ERROR else DbUpdateResult.SUCCESS)
        }
    }
}