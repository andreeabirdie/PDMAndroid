package ro.ubbcluj.ro.birdie.myapp.songs.songEdit

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_edit_song.*
import ro.ubbcluj.ro.birdie.myapp.R
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SongEditFragment : Fragment() {
    private lateinit var viewModel: SongEditViewModel
    private var song: Song? = null
    private var attemptAt: Long = 0
    private val REQUEST_PERMISSION = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
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
            attemptAt = Date().time
            title.setText(song?.title)
            streams.setText(song?.streams.toString())

            val df = SimpleDateFormat("dd.MM.yyyy")
            val date: Date = df.parse(song!!.releaseDate)
            val cal = Calendar.getInstance()
            cal.time = date

            datePicker.updateDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            hasAwards.isChecked = song?.hasAwards!!
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()
        fab.setOnClickListener {
            Log.v(TAG, "save song")
            song?.let {
                it.title = title.text.toString()
                it.streams = streams.text.toString().toInt()
                it.releaseDate =
                    "${datePicker.dayOfMonth}.${datePicker.month + 1}.${datePicker.year}"
                it.hasAwards = hasAwards.isChecked
                it.attemptUpdateAt = attemptAt
                viewModel.saveOrUpdateSong(it)
            }
        }
        if (!song?._id.isNullOrEmpty()) {
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener {
                song?.let { it1 -> viewModel.deleteItem(it1) }
                findNavController().navigate(R.id.fragment_song_list)
            }
        }
        takePictureBtn.setOnClickListener { openCamera() }
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
            song = Song("", "", 0, "01-01-2020", false, AuthRepository.getUsername(), "", 0, "");
        } else {
            viewModel.getSongById(id).observe(viewLifecycleOwner, {
                Log.v(TAG, "update items")
                if (it != null) {
                    song = it
                    title.setText(it.title)
                    streams.setText(it.streams.toString())

                    val df = SimpleDateFormat("dd.MM.yyyy")
                    val date: Date = df.parse(song!!.releaseDate)
                    val cal = Calendar.getInstance()
                    cal.time = date

                    datePicker.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    hasAwards.isChecked = it.hasAwards
                    song?.picturePath?.let { albumPicture.setImageURI(Uri.parse(song?.picturePath)) }
                }
            })
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION
            )
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createCapturedPhoto()
                } catch (ex: IOException) {
                    null
                }
                Log.d(TAG, "photofile $photoFile")
                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "ro.ubbcluj.ro.birdie.myapp.fileprovider",
                        it
                    )
                    Log.d(TAG, "photoURI: $photoURI");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var f = File.createTempFile("PHOTO_${timestamp}", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
        song?.picturePath = currentPhotoPath
        return f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                val uri = Uri.parse(currentPhotoPath)
                albumPicture.setImageURI(uri)
            }
        }
    }
}
