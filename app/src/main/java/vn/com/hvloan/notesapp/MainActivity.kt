package vn.com.hvloan.notesapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE_ADD_NOTE = 1
    lateinit var imageAddNoteMain: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponent()
        actionComponent()
    }

    private fun actionComponent() {
        imageAddNoteMain.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }
    }

    private fun initComponent() {
        imageAddNoteMain = findViewById(R.id.imageAddNoteMain)
    }
}