package vn.com.hvloan.notesapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import vn.com.hvloan.notesapp.R
import vn.com.hvloan.notesapp.database.NotesDatabase
import vn.com.hvloan.notesapp.entities.Note
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteActivity : AppCompatActivity() {

    private val TAG = CreateNoteActivity::class.java.simpleName

    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2

    private lateinit var imageBack: ImageView
    lateinit var inputNoteTitle: EditText
    lateinit var inputNoteSubtitle: EditText
    lateinit var inputNoteText: EditText
    lateinit var textDateTime: TextView
    lateinit var viewSubtitleIndicator: View
    lateinit var imageNote: ImageView
    lateinit var textWebURL: TextView
    lateinit var layoutWebURL: LinearLayout
    lateinit var imageSave: ImageView
    private lateinit var imageRemoveWebURL: ImageView
    lateinit var imageRemoveImage: ImageView
    lateinit var layoutMiscellaneous: LinearLayout

    private var dialogAddURL: AlertDialog? = null
    private var dialogDeleteNote: AlertDialog? = null

    lateinit var alreadyAvailableNote: Note

    var selectedNoteColor = "#333333"
    var selectedImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        initComponent()
        actionComponent()

        textDateTime.text = SimpleDateFormat(
            "EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()
        ).format(Date().time)

        if (intent.getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (intent.getSerializableExtra("note") as Note?)!!
            setViewOrUpdateNote()
        }

        if (intent.getBooleanExtra("isFromQuickActions", false)) {
            val type = intent.getStringExtra("quickActionType")
            if (type != null) {
                if (type == "image") {
                    selectedImagePath = intent.getStringExtra("imagePath")!!
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                    imageNote.visibility = View.VISIBLE
                    imageRemoveImage.visibility = View.VISIBLE
                } else if (type == "URL") {
                    textWebURL.text = intent.getStringExtra("URL")
                    layoutWebURL.visibility = View.VISIBLE
                }
            }
        }

        initMiscellaneous()
        setSubtitleIndicatorColor()

    }

    private fun setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.title)
        inputNoteSubtitle.setText(alreadyAvailableNote.subtitle)
        inputNoteText.setText(alreadyAvailableNote.noteText)
        textDateTime.text = alreadyAvailableNote.dateTime
        val imagePathStr = alreadyAvailableNote.imagePath
        if (imagePathStr.trim { it <= ' ' }.isNotEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(imagePathStr))
            imageNote.visibility = View.VISIBLE
            imageRemoveImage.visibility = View.VISIBLE
            selectedImagePath = imagePathStr
        }
        val webLinkStr = alreadyAvailableNote.webLink
        if (webLinkStr.trim { it <= ' ' }.isNotEmpty()) {
            textWebURL.text = alreadyAvailableNote.webLink
            layoutWebURL.visibility = View.VISIBLE
        }
    }

    private fun saveNote() {
        val noteTitle = inputNoteTitle.text.toString().trim { it <= ' ' }
        val noteSubtitle = inputNoteSubtitle.text.toString().trim { it <= ' ' }
        val noteText = inputNoteText.text.toString().trim { it <= ' ' }
        val dateTimeStr = textDateTime.text.toString().trim { it <= ' ' }
        if (noteTitle.isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show()
            return
        } else if (noteSubtitle.isEmpty() && noteText.isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show()
            return
        }
        val note = Note()
        note.title = noteTitle
        note.subtitle = noteSubtitle
        note.noteText = noteText
        note.dateTime = dateTimeStr
        note.color = selectedNoteColor
        note.imagePath = selectedImagePath
        if (layoutWebURL.visibility == View.VISIBLE) {
            note.webLink = textWebURL.text.toString()
        }
        note.id = alreadyAvailableNote.id
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask : AsyncTask<Void?, Void?, Void?>() {

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                val intent = Intent()
                setResult(RESULT_OK, intent)
                finish()
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                NotesDatabase.getNotesDatabase(applicationContext)!!.noteDao()!!.insertNote(note)
                return null
            }
        }
        SaveNoteTask().execute()
    }

    private fun initMiscellaneous() {
        val bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous)

        layoutMiscellaneous.findViewById<View>(R.id.textMiscellaneous).setOnClickListener {
                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }

        val imageColor1 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor1)
        val imageColor2 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor2)
        val imageColor3 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor3)
        val imageColor4 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor4)
        val imageColor5 = layoutMiscellaneous.findViewById<ImageView>(R.id.imageColor5)

        layoutMiscellaneous.findViewById<View>(R.id.viewColor1).setOnClickListener {
            selectedNoteColor = "#333333"
            imageColor1.setImageResource(R.drawable.ic_done)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor2).setOnClickListener {
            selectedNoteColor = "#FDBE3B"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(R.drawable.ic_done)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor3).setOnClickListener {
            selectedNoteColor = "#FF4842"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(R.drawable.ic_done)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor4).setOnClickListener {
            selectedNoteColor = "#3A52FC"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(R.drawable.ic_done)
            imageColor5.setImageResource(0)
            setSubtitleIndicatorColor()
        }
        layoutMiscellaneous.findViewById<View>(R.id.viewColor5).setOnClickListener {
            selectedNoteColor = "#000000"
            imageColor1.setImageResource(0)
            imageColor2.setImageResource(0)
            imageColor3.setImageResource(0)
            imageColor4.setImageResource(0)
            imageColor5.setImageResource(R.drawable.ic_done)
            setSubtitleIndicatorColor()
        }
        val noteColorCode = alreadyAvailableNote.color
        if (noteColorCode.trim { it <= ' ' }.isNotEmpty()) {
            when (noteColorCode) {
                "#FDBE3B" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor2)
                    .performClick()
                "#FF4842" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor3)
                    .performClick()
                "#3A52FC" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor4)
                    .performClick()
                "#000000" -> layoutMiscellaneous.findViewById<View>(R.id.viewColor5)
                    .performClick()
            }
        }
        layoutMiscellaneous.findViewById<View>(R.id.layoutAddImage).setOnClickListener { v: View? ->
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@CreateNoteActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                selectImage()
            }
        }
        layoutMiscellaneous.findViewById<View>(R.id.layoutAddUrl).setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showAddURLDialog()
        }
        layoutMiscellaneous.findViewById<View>(R.id.layoutDeleteNote).visibility = View.VISIBLE
        layoutMiscellaneous.findViewById<View>(R.id.layoutDeleteNote)
            .setOnClickListener {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                showDeleteNoteDialog()
            }
    }

    private fun showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            val builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view = LayoutInflater.from(this).inflate(
                R.layout.layout_delete_note,
                findViewById<View>(R.id.layoutDeleteNoteContainer) as ViewGroup
            )
            builder.setView(view)
            dialogDeleteNote = builder.create()
            if (dialogDeleteNote?.window != null) {
                dialogDeleteNote?.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            view.findViewById<View>(R.id.textDeleteNote).setOnClickListener { v: View? ->
                @SuppressLint("StaticFieldLeak")
                class DeleteNoteTask : AsyncTask<Void?, Void?, Void>() {

                    override fun onPostExecute(aVoid: Void) {
                        super.onPostExecute(aVoid)
                        val intent = Intent()
                        intent.putExtra("isNoteDeleted", true)
                        setResult(RESULT_OK, intent)
                        dialogDeleteNote?.dismiss()
                        finish()
                    }

                    override fun doInBackground(vararg p0: Void?): Void? {
                        NotesDatabase.getNotesDatabase(applicationContext)!!.noteDao()!!.deleteNote(alreadyAvailableNote)
                        return null
                    }
                }
                DeleteNoteTask().execute()
            }
            view.findViewById<View>(R.id.textCancel)
                .setOnClickListener {
                    dialogDeleteNote?.dismiss()
                }
        }
        dialogDeleteNote!!.show()
    }

    private fun setSubtitleIndicatorColor() {
        val gradientDrawable = viewSubtitleIndicator.background as GradientDrawable
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor))
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                if (selectedImageUri != null) {
                    try {
                        Glide.with(this@CreateNoteActivity)
                            .load(selectedImageUri)
                            .into(imageNote)
                        imageNote.visibility = View.VISIBLE
                        findViewById<View>(R.id.imageRemoveImage).visibility = View.VISIBLE
                        selectedImagePath = getPathFromUri(selectedImageUri)
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
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

    private fun showAddURLDialog() {
        if (dialogAddURL == null) {
            val builder = AlertDialog.Builder(this@CreateNoteActivity)
            val view = LayoutInflater.from(this)
                .inflate(R.layout.layout_add_url, findViewById(R.id.layoutAddUrlContainer))
            builder.setView(view)
            dialogAddURL = builder.create()
            if (dialogAddURL?.window != null) {
                dialogAddURL?.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            val inputURL = view.findViewById<EditText>(R.id.inputURL)
            inputURL.requestFocus()
            view.findViewById<View>(R.id.textAdd).setOnClickListener { v: View? ->
                val inputURLStr = inputURL.text.toString().trim { it <= ' ' }
                if (inputURLStr.isEmpty()) {
                    Toast.makeText(this@CreateNoteActivity, "Enter URL", Toast.LENGTH_SHORT).show()
                } else if (!Patterns.WEB_URL.matcher(inputURLStr).matches()) {
                    Toast.makeText(this@CreateNoteActivity, "Enter valid URL", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    textWebURL.text = inputURL.text.toString()
                    layoutWebURL.visibility = View.VISIBLE
                    dialogAddURL?.dismiss()
                }
            }
            view.findViewById<View>(R.id.textCancel)
                .setOnClickListener {
                    dialogAddURL?.dismiss()
                }
        }
        dialogAddURL?.show()
    }

    private fun actionComponent() {
        imageBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        imageSave.setOnClickListener {
            saveNote()
        }

        imageRemoveWebURL.setOnClickListener {
            textWebURL.text = null
            layoutWebURL.visibility = View.GONE
        }

        imageRemoveImage.setOnClickListener {
            imageNote.setImageBitmap(null)
            imageNote.visibility = View.GONE
            imageRemoveImage.visibility = View.GONE
            selectedImagePath = ""
        }
    }

    private fun initComponent() {
        imageBack = findViewById(R.id.imageBack)
        inputNoteTitle = findViewById(R.id.inputNoteTitle)
        inputNoteSubtitle = findViewById(R.id.inputNoteSubtitle)
        inputNoteText = findViewById(R.id.inputNoteText)
        textDateTime = findViewById(R.id.textDateTime)
        viewSubtitleIndicator = findViewById(R.id.viewSubtitleIndicator)
        imageNote = findViewById(R.id.imageNote)
        imageSave = findViewById(R.id.imageSave)
        textWebURL = findViewById(R.id.textWebURL)
        layoutWebURL = findViewById(R.id.layoutWebURL)
        imageRemoveWebURL = findViewById(R.id.imageRemoveWebURL)
        imageRemoveImage = findViewById(R.id.imageRemoveImage)
        layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous)
        alreadyAvailableNote = Note()
    }
}