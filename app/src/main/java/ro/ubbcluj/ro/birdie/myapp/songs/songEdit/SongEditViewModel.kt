package ro.ubbcluj.ro.birdie.myapp.songs.songEdit

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepository
import ro.ubbcluj.ro.birdie.myapp.songs.data.local.SongDatabase

class SongEditViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val songRepository: SongRepository

    init {
        val songDao = SongDatabase.getDatabase(application, viewModelScope).songDao()
        songRepository = SongRepository(songDao)
    }

    fun getSongById(songId: String): LiveData<Song> {
        Log.v(TAG, "getSongById...")
        return songRepository.getById(songId)
    }

    fun saveOrUpdateSong(song: Song) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateSong...");
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Song>
            Log.v("qwerty","saving song $song")
            if (song._id.isNotEmpty()) {
                result = songRepository.update(song)
            } else {
                result = songRepository.save(song)
            }
            when (result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }

    fun deleteItem(songId: String) {
        viewModelScope.launch {
            Log.v(TAG, "deleteItem...");
            val result = songRepository.delete(songId)
            when (result) {
                is Result.Success -> {
                    Log.d(TAG, "deleteItem succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "deleteItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
        }
    }
}
