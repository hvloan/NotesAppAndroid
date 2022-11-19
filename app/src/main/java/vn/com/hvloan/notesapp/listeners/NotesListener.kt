package vn.com.hvloan.notesapp.listeners

import vn.com.hvloan.notesapp.entities.Note

interface NotesListener {
    fun onNoteClicked(note: Note?, position: Int)
}
