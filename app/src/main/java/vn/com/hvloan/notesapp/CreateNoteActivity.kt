package vn.com.hvloan.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class CreateNoteActivity : AppCompatActivity() {

    lateinit var imageBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        initComponent()
        actionComponent()
    }

    private fun actionComponent() {
        imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initComponent() {
        imageBack = findViewById(R.id.imageBack)
    }
}