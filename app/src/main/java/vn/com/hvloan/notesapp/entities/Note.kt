package vn.com.hvloan.notesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes")
class Note : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "dateTime")
    var dateTime: String = ""

    @ColumnInfo(name = "subtitle")
    var subtitle: String = ""

    @ColumnInfo(name = "noteText")
    var noteText: String = ""

    @ColumnInfo(name = "imagePath")
    var imagePath: String = ""

    @ColumnInfo(name = "color")
    var color: String = ""

    @ColumnInfo(name = "webLink")
    var webLink: String = ""

    override fun toString(): String {
        return "$title：$dateTime"
    }
}