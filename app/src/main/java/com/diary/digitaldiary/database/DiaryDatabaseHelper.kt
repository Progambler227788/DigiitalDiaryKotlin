package com.diary.digitaldiary.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.diary.digitaldiary.models.DiaryEntry

class DiaryDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Diary.db"
    }

    object DiaryEntryContract {
        object DiaryEntry : BaseColumns {
            const val TABLE_NAME = "diary_entry"
            const val COLUMN_TITLE = "title"
            const val COLUMN_LOCATION = "location"
            const val COLUMN_NOTE = "note"
            const val COLUMN_PHOTO_PATH = "photo_path"
            const val COLUMN_VOICE_PATH = "voice_path"
        }
    }

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${DiaryEntryContract.DiaryEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "${DiaryEntryContract.DiaryEntry.COLUMN_TITLE} TEXT," +
                "${DiaryEntryContract.DiaryEntry.COLUMN_LOCATION} TEXT," +
                "${DiaryEntryContract.DiaryEntry.COLUMN_NOTE} TEXT," +
                "${DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH} TEXT," +
                "${DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH} TEXT)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${DiaryEntryContract.DiaryEntry.TABLE_NAME}"
    fun updateDiaryEntry(
        id: Long,
        title: String,
        location: String,
        note: String,
        photoPath: String?,
        voicePath: String?
    ): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(DiaryEntryContract.DiaryEntry.COLUMN_TITLE, title)
            put(DiaryEntryContract.DiaryEntry.COLUMN_LOCATION, location)
            put(DiaryEntryContract.DiaryEntry.COLUMN_NOTE, note)
            put(DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH, photoPath)
            put(DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH, voicePath)
        }

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.update(
            DiaryEntryContract.DiaryEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )
    }

    fun getDiaryEntry(diaryEntryId: Long): DiaryEntry? {
        val db = readableDatabase
        var diaryEntry: DiaryEntry? = null

        val projection = arrayOf(
            BaseColumns._ID,
            DiaryEntryContract.DiaryEntry.COLUMN_TITLE,
            DiaryEntryContract.DiaryEntry.COLUMN_LOCATION,
            DiaryEntryContract.DiaryEntry.COLUMN_NOTE,
            DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH,
            DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH
        )

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(diaryEntryId.toString())

        val cursor = db.query(
            DiaryEntryContract.DiaryEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(BaseColumns._ID))
                val title = it.getString(it.getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_TITLE))
                val location = it.getString(it.getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_LOCATION))
                val note = it.getString(it.getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_NOTE))
                val photoPath = it.getString(it.getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH))
                val voicePath = it.getString(it.getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH))

                diaryEntry = DiaryEntry(id, title, location, note, photoPath, voicePath)
            }
        }

        Log.d("database voice", diaryEntry?.voicePath.toString())

        return diaryEntry
    }




    fun loadEntriesFromDatabase(): MutableList<DiaryEntry> {
        val diaryEntries = mutableListOf<DiaryEntry>()

        // Get a readable database
        val db = readableDatabase

        // Define projection for the columns you want to retrieve
        val projection = arrayOf(
            BaseColumns._ID,
            DiaryEntryContract.DiaryEntry.COLUMN_TITLE,
            DiaryEntryContract.DiaryEntry.COLUMN_LOCATION,
            DiaryEntryContract.DiaryEntry.COLUMN_NOTE,
            DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH,
            DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH
        )

        // Query the database
        val cursor = db.query(
            DiaryEntryContract.DiaryEntry.TABLE_NAME,  // The table to query
            projection,                                // The columns to return
            null,                                      // The columns for the WHERE clause
            null,                                      // The values for the WHERE clause
            null,                                      // don't group the rows
            null,                                      // don't filter by row groups
            null                                       // The sort order
        )

        // Iterate through the cursor and populate diaryEntries list
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val title = getString(getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_TITLE))
                val location = getString(getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_LOCATION))
                val note = getString(getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_NOTE))
                val photoPath = getString(getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH))
                val voicePath = getString(getColumnIndexOrThrow(DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH))

                val diaryEntry = DiaryEntry(id, title, location, note, photoPath, voicePath)
                diaryEntries.add(diaryEntry)
            }
        }

        // Close the cursor after use
        cursor.close()

        return diaryEntries
    }


    fun insertDiaryEntry(
        title: String,
        location: String,
        note: String,
        photoPath: String?,
        voicePath: String?
    ): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(DiaryEntryContract.DiaryEntry.COLUMN_TITLE, title)
            put(DiaryEntryContract.DiaryEntry.COLUMN_LOCATION, location)
            put(DiaryEntryContract.DiaryEntry.COLUMN_NOTE, note)
            put(DiaryEntryContract.DiaryEntry.COLUMN_PHOTO_PATH, photoPath)
            put(DiaryEntryContract.DiaryEntry.COLUMN_VOICE_PATH, voicePath)
        }

        return db.insert(DiaryEntryContract.DiaryEntry.TABLE_NAME, null, values)
    }
}
