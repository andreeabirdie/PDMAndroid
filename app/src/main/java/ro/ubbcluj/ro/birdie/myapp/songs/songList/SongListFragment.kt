package ro.ubbcluj.ro.birdie.myapp.songs.songList

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_song_list.*
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.TAG

class SongListFragment : Fragment() {
    private lateinit var songListAdapter: SongListAdapter
    private lateinit var songModel: SongListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn(requireContext())) {
            Log.d(TAG, "is not logged in")
            findNavController().navigate(R.id.fragment_login)
            return
        }
        setupSongList()
        fab.setOnClickListener {
            Log.v(TAG, "add new song")
            findNavController().navigate(R.id.fragment_edit_song)
        }
        logoutBtn.setOnClickListener {
            Log.v(TAG, "log out")
            AuthRepository.logout()
            findNavController().navigate(R.id.fragment_login)
        }
    }

    private fun setupSongList() {
        songListAdapter = SongListAdapter(this)
        item_list.adapter = songListAdapter
        songModel = ViewModelProvider(this).get(SongListViewModel::class.java)
        songModel.songs.observe(viewLifecycleOwner) { song ->
            Log.v(TAG, "update items")
            Log.d(TAG, "setupItemList items length: ${song.size}")
            songListAdapter.songs = song
        }
        songModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        songModel.loadingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
        songModel.refresh()

        search.doOnTextChanged { _, _, _, _ ->
            songModel.songs.observe(viewLifecycleOwner, { song ->
                songListAdapter.songs = song
                songListAdapter.songs =
                    songListAdapter.searchAndFilter(search.text.toString(), hasAwards.isChecked, noAwards.isChecked)
                songListAdapter.notifyDataSetChanged()
            })
        }

        hasAwards.setOnClickListener {
            if(hasAwards.isChecked) noAwards.isChecked = false
            songModel.songs.observe(viewLifecycleOwner, { song ->
                songListAdapter.songs = song
                songListAdapter.songs =
                    songListAdapter.searchAndFilter(search.text.toString(), hasAwards.isChecked, noAwards.isChecked)
                songListAdapter.notifyDataSetChanged()
            })
        }

        noAwards.setOnClickListener {
            if(noAwards.isChecked) hasAwards.isChecked = false
            songModel.songs.observe(viewLifecycleOwner, { song ->
                songListAdapter.songs = song
                songListAdapter.songs =
                    songListAdapter.searchAndFilter(search.text.toString(), hasAwards.isChecked, noAwards.isChecked)
                songListAdapter.notifyDataSetChanged()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}