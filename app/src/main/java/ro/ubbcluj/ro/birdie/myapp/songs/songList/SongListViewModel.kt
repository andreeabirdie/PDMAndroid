package ro.ubbcluj.ro.birdie.myapp.songs.songList

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepository
import ro.ubbcluj.ro.birdie.myapp.songs.data.local.SongDatabase

class SongListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val songs: LiveData<List<Song>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val songRepository: SongRepository

    init {
        val songDao = SongDatabase.getDatabase(application, viewModelScope).songDao()
        songRepository = SongRepository(songDao)
        songs = songRepository.songs
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = songRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }
}
