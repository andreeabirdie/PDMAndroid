package ro.ubbcluj.ro.birdie.myapp.songs.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey @ColumnInfo(name = "_id") val _id: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "streams") var streams: Int,
    @ColumnInfo(name = "releaseDate") var releaseDate: String,
    @ColumnInfo(name = "hasAwards") var hasAwards: Boolean,
    @ColumnInfo(name = "owner") var owner: String?,
    @ColumnInfo(name = "upToDate") var upToDate: Boolean?,
    @ColumnInfo(name = "action") var action: String?
) : Parcelable {
    override fun toString(): String = "$title $streams $releaseDate $hasAwards"

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val netDate = kotlin.math.floor(releaseDate.toDouble())
        return sdf.format(netDate)
    }
}

data class SongDTO(
    var title: String,
    var streams: Int,
    var releaseDate: String,
    var hasAwards: Boolean,
    var owner: String?
)