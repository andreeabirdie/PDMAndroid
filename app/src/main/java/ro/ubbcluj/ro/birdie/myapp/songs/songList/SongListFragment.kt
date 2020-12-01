package ro.ubbcluj.ro.birdie.myapp.songs.songList

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.fragment_song_list.*
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.Properties
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepoHelper
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepoWorker

class SongListFragment : Fragment() {
    private lateinit var songListAdapter: SongListAdapter
    private lateinit var viewModel: SongListViewModel

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SongRepoHelper.setViewLifecycleOwner(viewLifecycleOwner)
        Properties.instance.internetActive.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "sending offline actions to server")
            sendOfflineActionsToServer() })
    }

    private fun sendOfflineActionsToServer() {
        val songs = viewModel.songRepository.songDao.getAllSongs(AuthRepository.getUsername())
        songs.forEach { song ->
            if (song.action == null) {
                song.action = ""
            }
            if (song.action != "") {
                Log.d(TAG, "${song.title} needs ${song.action}")
                SongRepoHelper.setSong(song)
                var dataParam = Data.Builder().putString("operation", "save")
                when(song.action) {
                    "update" -> {
                        dataParam = Data.Builder().putString("operation", "update")
                    }
                    "delete" -> {
                        dataParam = Data.Builder().putString("operation", "delete")
                    }
                }
                val request = OneTimeWorkRequestBuilder<SongRepoWorker>()
                    .setInputData(dataParam.build())
                    .build()
                WorkManager.getInstance(requireContext()).enqueue(request)
            }
        }
    }

    private fun setupSongList() {
        songListAdapter = SongListAdapter(this)
        item_list.adapter = songListAdapter
        viewModel = ViewModelProvider(this).get(SongListViewModel::class.java)

        viewModel.songs.observe(viewLifecycleOwner) { song ->
            Log.v(TAG, "update items")
            Log.d(TAG, "setupItemList items length: ${song.size}")
            songListAdapter.songs = song.filter { it.action != "delete" }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.loadingError.observe(viewLifecycleOwner) { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.refresh()

        search.doOnTextChanged { _, _, _, _ ->
            viewModel.songs.observe(viewLifecycleOwner, { song ->
                songListAdapter.songs = song
                songListAdapter.songs =
                    songListAdapter.searchAndFilter(search.text.toString(), hasAwards.isChecked, noAwards.isChecked)
                songListAdapter.notifyDataSetChanged()
            })
        }

        hasAwards.setOnClickListener {
            if(hasAwards.isChecked) noAwards.isChecked = false
            viewModel.songs.observe(viewLifecycleOwner, { song ->
                songListAdapter.songs = song
                songListAdapter.songs =
                    songListAdapter.searchAndFilter(search.text.toString(), hasAwards.isChecked, noAwards.isChecked)
                songListAdapter.notifyDataSetChanged()
            })
        }

        noAwards.setOnClickListener {
            if(noAwards.isChecked) hasAwards.isChecked = false
            viewModel.songs.observe(viewLifecycleOwner, { song ->
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