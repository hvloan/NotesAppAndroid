package vn.com.hvloan.notesapp.dao

import androidx.room.*
import vn.com.hvloan.notesapp.entities.Note


@Dao
interface NoteDao {
    @get:Query("SELECT * FROM notes ORDER BY id DESC")
    val allNotes: List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note)

    @Delete
    fun deleteNote(note: Note)
}