package com.hallliu.passwordgenerator

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.HandlerThread
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.inject.Inject

fun Cursor.getStringByName(name: String): String =
        this.getString(this.getColumnIndex(name))

fun Cursor.getIntByName(name: String): Int =
        this.getInt(this.getColumnIndex(name))

class DbInterface @Inject constructor(val dbHelper: SiteDbHelper) {
    private val handler: Handler
    private val handlerThread = HandlerThread(this.javaClass.simpleName)

    lateinit var db: SQLiteDatabase

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        handler.post { db = dbHelper.writableDatabase }
    }

    companion object {
        enum class SaveSiteResult {
            SUCCESS, ALREADY_EXISTS, OTHER_ERROR
        }
        enum class UpdateSiteResult {
            SUCCESS, NO_SUCH_SITE, OTHER_ERROR
        }
    }

    fun getSitesLike(site: String, callback: (result: List<String>) -> Unit) {
        getSitesLike(site, arrayOf(COLUMN_SITE_NAME),
                {it.getStringByName(COLUMN_SITE_NAME)}, callback)
    }

    fun <T> getSitesLike(site: String, columns: Array<String>,
                         factory: (cursor: Cursor) -> T, callback: (result: List<T>) -> Unit) {
        handler.post {
            val whereClause = "$COLUMN_SITE_NAME LIKE ?"
            val whereArgs = arrayOf("%$site%")
            db.query(MAIN_TABLE_NAME, columns, whereClause,
                    whereArgs, null, null, null).use { cursor ->
                val sites = mutableListOf<T>()
                while (cursor.moveToNext()) {
                    sites.add(factory(cursor))
                }
                callback(sites)
            }
        }
    }

    fun getPwSpecForSite(site: String, callback: (result: PasswordSpecification?) -> Unit) {
        handler.post {
            val columns = arrayOf(COLUMN_ID, COLUMN_SITE_NAME, COLUMN_PERMITTED_CHARS,
                    COLUMN_PASSWORD_LENGTH, COLUMN_VERSION)
            val whereClause = COLUMN_SITE_NAME + " = ?"
            val whereArgs = arrayOf(site)
            db.query(MAIN_TABLE_NAME, columns, whereClause, whereArgs, null, null, null)
                    .use { cursor ->
                if (cursor.count == 0) {
                    callback(null)
                    return@post
                }
                cursor.moveToFirst()

                val requirementPatterns =
                        getRequirementsPatternForSite(cursor.getIntByName(COLUMN_ID))

                callback(PasswordSpecification(site,
                        permittedChars = cursor.getStringByName(COLUMN_PERMITTED_CHARS),
                        requirements = requirementPatterns,
                        pwLength = cursor.getIntByName(COLUMN_PASSWORD_LENGTH),
                        pwVersion = cursor.getIntByName(COLUMN_VERSION)))
            }
        }
    }

    private fun getRequirementsPatternForSite(siteId: Int): List<Pattern> {
        val columnsForPatterns = arrayOf(COLUMN_PATTERN)
        val whereClauseForPatterns = COLUMN_SITE_ID + " = ?"
        val patterns = mutableListOf<Pattern>()
        db.query(PATTERNS_TABLE_NAME, columnsForPatterns, whereClauseForPatterns,
                arrayOf(siteId.toString()), null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                try {
                    patterns.add(Pattern.compile(cursor.getStringByName(COLUMN_PATTERN)))
                } catch (e: PatternSyntaxException) {
                    // Ignore it and continue.
                }
            }
        }
        return patterns
    }

    fun deleteSites(sites: Collection<String>, callback: (Int) -> Unit) {
        handler.post {
            val columns = arrayOf(COLUMN_ID)
            val whereClause = COLUMN_SITE_NAME + " = ?"
            val whereArgs = sites.toTypedArray()
            db.query(MAIN_TABLE_NAME, columns, whereClause, whereArgs, null, null, null)
                    .use { cursor ->
                        val ids = (1..cursor.count).map {
                            cursor.moveToNext()
                            cursor.getIntByName(COLUMN_ID).toString()
                        }.toTypedArray()
                        val deleteWhereMain = COLUMN_ID + " = ?"
                        val deleteWhereRequirements = COLUMN_SITE_ID + " = ?"
                        val numSites = db.delete(MAIN_TABLE_NAME, deleteWhereMain, ids)
                        db.delete(PATTERNS_TABLE_NAME, deleteWhereRequirements, ids)
                        callback(numSites)
            }
        }
    }

    fun updateSiteInDb(pwSpec: PasswordSpecification, callback: (UpdateSiteResult) -> Unit) {
        deleteSites(listOf(pwSpec.siteName)) {
            when(it) {
                0 -> callback(UpdateSiteResult.NO_SUCH_SITE)
                else -> {
                    saveSiteInDb(pwSpec) {
                        callback(when(it) {
                            SaveSiteResult.SUCCESS -> UpdateSiteResult.SUCCESS
                            else -> UpdateSiteResult.OTHER_ERROR
                        })
                    }
                }
            }
        }
    }

    fun saveSiteInDb(pwSpec: PasswordSpecification, callback: (SaveSiteResult) -> Unit) {
        handler.post {
            // Check for existing site first
            db.query(MAIN_TABLE_NAME,
                    arrayOf(COLUMN_SITE_NAME),
                    COLUMN_SITE_NAME + " = ?",
                    arrayOf(pwSpec.siteName),
                    null, null, null).use { cursor ->
                if (cursor.count > 0) {
                    callback(SaveSiteResult.ALREADY_EXISTS)
                    return@post
                }
                val mainTableValues = ContentValues()
                mainTableValues.put(COLUMN_SITE_NAME, pwSpec.siteName)
                mainTableValues.put(COLUMN_PERMITTED_CHARS, pwSpec.permittedChars)
                mainTableValues.put(COLUMN_PASSWORD_LENGTH, pwSpec.pwLength)
                mainTableValues.put(COLUMN_VERSION, pwSpec.pwVersion)
                mainTableValues.put(COLUMN_LAST_MODIFIED, System.currentTimeMillis() / 1000)
                db.beginTransaction()
                try {
                    val row = db.insert(MAIN_TABLE_NAME, null, mainTableValues)
                    if (row == -1L) {
                        callback(SaveSiteResult.OTHER_ERROR)
                        return@post
                    }

                    pwSpec.requirements.forEach {
                        val requirementTableValues = ContentValues()
                        requirementTableValues.put(COLUMN_SITE_ID, row)
                        requirementTableValues.put(COLUMN_PATTERN, it.toString())
                        if (db.insert(PATTERNS_TABLE_NAME, null, requirementTableValues) == -1L) {
                            callback(SaveSiteResult.OTHER_ERROR)
                            return@post
                        }
                    }
                    callback(SaveSiteResult.SUCCESS)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
}