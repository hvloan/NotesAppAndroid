package vn.com.hvloan.notesapp.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import vn.com.hvloan.notesapp.activities.MainActivity
import vn.com.hvloan.notesapp.entities.Note
import vn.com.hvloan.notesapp.listeners.NotesListener
import vn.com.hvloan.notesapp.R
import java.util.*


class NotesAdapter(notes: List<Note>, notesListener: MainActivity) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {
    private var notes: List<Note>
    private val notesSource: List<Note>
    private val notesListener: NotesListener
    private var timer: Timer? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_container_note, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.setNote(notes[position])
        holder.layoutNote.setOnClickListener {
            notesListener.onNoteClicked(
                notes[position],
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textTitle: TextView
        var textSubtitle: TextView
        var textDateTime: TextView
        var layoutNote: LinearLayout
        var imageNote: RoundedImageView
        fun setNote(note: Note) {
            textTitle.text = note.title
            if (note.subtitle.trim().isEmpty()) {
                textSubtitle.visibility = View.GONE
            } else {
                textSubtitle.text = note.subtitle
            }
            textDateTime.text = note.dateTime
            val gradientDrawable = layoutNote.background as GradientDrawable
            gradientDrawable.setColor(Color.parseColor(note.color))
            imageNote.setImageBitmap(BitmapFactory.decodeFile(note.imagePath))
            imageNote.visibility = View.VISIBLE
        }

        init {
            textTitle = itemView.findViewById(R.id.textTitle)
            textSubtitle = itemView.findViewById(R.id.textSubtitle)
            textDateTime = itemView.findViewById(R.id.textDateTime)
            layoutNote = itemView.findViewById(R.id.layoutNote)
            imageNote = itemView.findViewById(R.id.imageNote)
        }
    }

    fun searchNotes(searchKeyword: String) {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                notes = if (searchKeyword.trim { it <= ' ' }.isEmpty()) {
                    notesSource
                } else {
                    val temp: ArrayList<Note> = ArrayList()
                    for (note in notesSource) {
                        if (note.title.lowercase(Locale.getDefault())
                                .contains(searchKeyword.lowercase(Locale.getDefault())) ||
                            note.subtitle.lowercase(Locale.getDefault()).contains(
                                searchKeyword.lowercase(
                                    Locale.getDefault()
                                )
                            ) ||
                            note.noteText.lowercase(Locale.getDefault()).contains(
                                searchKeyword.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        ) {
                            temp.add(note)
                        }
                    }
                    temp
                }
                Handler(Looper.getMainLooper()).post { notifyDataSetChanged() }
            }
        }, 500)
    }

    fun cancelTimer() {
        timer?.cancel()
    }

    init {
        this.notes = notes
        this.notesListener = notesListener
        notesSource = notes
    }
}