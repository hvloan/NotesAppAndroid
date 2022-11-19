package vn.com.hvloan.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vn.com.hvloan.notesapp.dao.NoteDao
import vn.com.hvloan.notesapp.entities.Note


@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao?

    companion object {
        private var notesDatabase: NotesDatabase? = null
        @Synchronized
        fun getNotesDatabase(context: Context?): NotesDatabase? {
            if (notesDatabase == null) {
                notesDatabase = context?.let {
                    Room.databaseBuilder(
                        it,
                        NotesDatabase::class.java,
                        "notes_db"
                    ).build()
                }
            }
            return notesDatabase
        }
    }
}