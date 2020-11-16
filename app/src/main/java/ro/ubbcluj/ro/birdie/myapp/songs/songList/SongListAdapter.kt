package ro.ubbcluj.ro.birdie.myapp.songs.songList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_song.view.*
import kotlinx.android.synthetic.main.view_song.view.streams
import kotlinx.android.synthetic.main.view_song.view.title
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song

class SongListAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    var songs = emptyList<Song>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onSongClick: View.OnClickListener

    init {
        onSongClick = View.OnClickListener { view ->
            val song = view.tag as Song
            fragment.findNavController()
                .navigate(
                    R.id.action_SongListFragment_to_SongEditFragment,
                    bundleOf("song" to song)
                )
        }
    }

    fun searchAndFilter(substring: String, hasAwards: Boolean, noAwards : Boolean): MutableList<Song> {
        val filteredList: MutableList<Song> = ArrayList()
        val substring = substring.toLowerCase().trim()
        for (song in songs) {
            Log.v("qwerty", "$substring ${song.title}")
            if (substring.isNotEmpty() && !song.title.toLowerCase().contains(substring))
                continue
            if(hasAwards && song.hasAwards!=hasAwards)
                continue
            if(noAwards && song.hasAwards==noAwards)
                continue
            filteredList.add(song)
        }
        return filteredList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_song, parent, false)
        Log.v(TAG, "onCreateViewHolder")
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(holder, position)
    }

    override fun getItemCount() = songs.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.title
        private val streams: TextView = view.streams
        private val releaseDate: TextView = view.releaseDate
        private val starIc: ImageView = view.starAwards

        fun bind(holder: ViewHolder, position: Int) {
            val song = songs[position]

            with(holder) {
                itemView.tag = song
                title.text = song.title
                streams.text = song.streams.toString()
                releaseDate.text = song.releaseDate
                itemView.setOnClickListener(onSongClick)
                if(song.hasAwards) starIc.visibility = View.VISIBLE
                else starIc.visibility = View.GONE
            }
        }
    }
}
