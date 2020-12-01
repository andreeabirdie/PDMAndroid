package ro.ubbcluj.ro.birdie.myapp.songs.songEdit

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_edit_song.*
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import java.text.SimpleDateFormat
import java.util.*

class SongEditFragment : Fragment() {
    private lateinit var viewModel: SongEditViewModel
    private var song: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_edit_song, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
        song = arguments?.getParcelable("song")
        song?.let {
            title.setText(song?.title)
            streams.setText(song?.streams.toString())

            val df  = SimpleDateFormat("dd.MM.yyyy")
            val date : Date = df.parse(song!!.releaseDate)
            val cal = Calendar.getInstance()
            cal.time = date

            datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            hasAwards.isChecked = song?.hasAwards!!
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab.setOnClickListener {
            Log.v(TAG, "save song")
            song?.let{
                it.title = title.text.toString()
                it.streams = streams.text.toString().toInt()
                it.releaseDate = "${datePicker.dayOfMonth}.${datePicker.month+1}.${datePicker.year}"
                it.hasAwards = hasAwards.isChecked
                viewModel.saveOrUpdateSong(it)
            }
        }
        if(!song?._id.isNullOrEmpty()) {
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener {
                song?.let { it1 -> viewModel.deleteItem(it1) }
                findNavController().navigate(R.id.fragment_song_list)
            }}
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(SongEditViewModel::class.java)
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().popBackStack()
            }
        })
        val id = song?._id
        if (id == null) {
            song = Song("", "", 0, "01-01-2020", false, AuthRepository.getUsername(), true, null);
        } else {
            viewModel.getSongById(id).observe(viewLifecycleOwner, {
                Log.v(TAG, "update items")
                if (it != null) {
                    song = it
                    title.setText(it.title)
                    streams.setText(it.streams.toString())

                    val df  = SimpleDateFormat("dd.MM.yyyy")
                    val date : Date = df.parse(song!!.releaseDate)
                    val cal = Calendar.getInstance()
                    cal.time = date

                    datePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                    hasAwards.isChecked = it.hasAwards
                }
            })
        }
    }
}
