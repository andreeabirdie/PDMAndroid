package ro.ubbcluj.ro.birdie.myapp.todo.songs

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_song.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.todo.data.FunctionHolder
import ro.ubbcluj.ro.birdie.myapp.todo.data.Song
import ro.ubbcluj.ro.birdie.myapp.todo.data.SongRepository
import java.text.SimpleDateFormat

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
        SongRepository.setFunctionHolder(object : FunctionHolder {
            override fun function() {
                MainScope().launch {
                    withContext(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }
        })
        onSongClick = View.OnClickListener { view ->
            val song = view.tag as Song
            fragment.findNavController()
                .navigate(
                    R.id.action_SongListFragment_to_SongEditFragment,
                    bundleOf("song" to song)
                )
        }
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
        private val hasAwards: TextView = view.hasAwards

        fun bind(holder: ViewHolder, position: Int) {
            val song = songs[position]

            with(holder) {
                itemView.tag = song
                title.text = song.title
                streams.text = song.streams.toString()
                releaseDate.text = song.getFormattedDate()
                hasAwards.text = song.hasAwards.toString()
                itemView.setOnClickListener(onSongClick)
            }
        }
    }
}
