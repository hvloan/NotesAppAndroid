package vn.com.hvloan.notesapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vn.com.hvloan.notesapp.adapter.NotesAdapter
import vn.com.hvloan.notesapp.database.NotesDatabase
import vn.com.hvloan.notesapp.entities.Note
import vn.com.hvloan.notesapp.listeners.NotesListener
import vn.com.hvloan.notesapp.R


class MainActivity : AppCompatActivity(), NotesListener {


    val REQUEST_CODE_ADD_NOTE = 1
    val REQUEST_CODE_UPDATE_NOTE = 2
    val REQUEST_CODE_SHOW_NOTES = 3
    val REQUEST_CODE_SELECT_IMAGE = 4
    val REQUEST_CODE_STORAGE_PERMISSION = 5

    private lateinit var imageAddNoteMain: ImageView
    private lateinit var inputSearch: EditText
    private lateinit var imageAddNote: ImageView
    private lateinit var imageAddImage: ImageView

    lateinit var notesRecyclerView: RecyclerView
    lateinit var noteList: ArrayList<Note>
    lateinit var notesAdapter: NotesAdapter
    var noteClickedPosition = -1
    private lateinit var dialogAddURL: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponent()
        getNotes(REQUEST_CODE_SHOW_NOTES, false)
        actionComponent()
        setupNoteRecyclerView()
    }

    private fun setupNoteRecyclerView() {
        notesAdapter = NotesAdapter(noteList, this)
        notesRecyclerView.adapter = notesAdapter
        notesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun actionComponent() {
        imageAddNoteMain.setOnClickListener {
            startActivityForResult(
                Intent(this, CreateNoteActivity::class.java),
                REQUEST_CODE_ADD_NOTE
            )
        }

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                notesAdapter.cancelTimer()
            }

            override fun afterTextChanged(s: Editable) {
                if (noteList.isNotEmpty()) {
                    notesAdapter.searchNotes(s.toString())
                }
            }
        })

        imageAddImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                selectImage()
            }
        }

        imageAddNote.setOnClickListener {
            showAddURLDialog()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPathFromUri(contentUri: Uri?): String {
        var filePath = ""
        val cursor: Cursor? = contentUri?.let { contentResolver.query(it, null, null, null, null) }
        if (cursor == null) {
            if (contentUri != null) {
                filePath = contentUri.path.toString()
            }
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onNoteClicked(note: Note?, position: Int) {
        noteClickedPosition = position
        val intent = Intent(applicationContext, CreateNoteActivity::class.java)
        intent.putExtra("isViewOrUpdate", true)
        intent.putExtra("note", note)
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE)
    }

    private fun getNotes(requestCode: Int, isNoteDeleted: Boolean) {
        @SuppressLint("StaticFieldLeak")
        class GetNoteTask : AsyncTask<Void?, Void?, List<Note>>() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onPostExecute(notes: List<Note>) {
                super.onPostExecute(notes)
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes)
                    notesAdapter.notifyDataSetChanged()
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes[0])
                    notesAdapter.notifyItemInserted(0)
                    notesRecyclerView.smoothScrollToPosition(0)
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.removeAt(noteClickedPosition)
                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition)
                    } else {
                        noteList.add(noteClickedPosition, notes[noteClickedPosition])
                        notesAdapter.notifyItemChanged(noteClickedPosition)
                    }
                }
            }

            override fun doInBackground(vararg p0: Void?): List<Note> {
                return NotesDatabase.getNotesDatabase(applicationContext)?.noteDao()!!.allNotes
            }
        }
        GetNoteTask().execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false)
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false))
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    try {
                        val selectedImagePath = getPathFromUri(selectedImageUri)
                        val intent = Intent(applicationContext, CreateNoteActivity::class.java)
                        intent.putExtra("isFromQuickActions", true)
                        intent.putExtra("quickActionType", "image")
                        intent.putExtra("imagePath", selectedImagePath)
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddURLDialog() {
        dialogAddURL.show()
    }

    private fun initComponent() {
        imageAddNoteMain = findViewById(R.id.imageAddNoteMain)
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        inputSearch = findViewById(R.id.inputSearch)
        noteList = ArrayList()
        imageAddNote = findViewById(R.id.imageAddNote)
        imageAddImage = findViewById(R.id.imageAddImage)
    }
}