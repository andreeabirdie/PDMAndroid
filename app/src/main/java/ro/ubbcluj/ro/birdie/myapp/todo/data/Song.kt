package ro.ubbcluj.ro.birdie.myapp.todo.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat

@Parcelize
data class Song(
    val id: String,
    var title: String,
    var streams: Int,
    var releaseDate: String,
    var hasAwards: Boolean
) : Parcelable {
    override fun toString(): String = "$title $streams $releaseDate $hasAwards"

    fun getFormattedDate() : String {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val netDate = kotlin.math.floor(releaseDate.toDouble())
        return sdf.format(netDate)
    }
}
