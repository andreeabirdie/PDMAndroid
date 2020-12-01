package ro.ubbcluj.ro.birdie.myapp.songs.songList

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepoHelper
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongRepository
import ro.ubbcluj.ro.birdie.myapp.songs.data.local.SongDatabase
import ro.ubbcluj.ro.birdie.myapp.songs.data.remote.RemoteDataSource

class SongListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val songRepository: SongRepository
    var songs: LiveData<List<Song>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    init {
        val songDao = SongDatabase.getDatabase(application).songDao()
        songRepository = SongRepository(songDao)
        songs = songRepository.songs

        SongRepoHelper.setSongRepo(songRepository)

        val request = Request.Builder().url("ws://192.168.0.104:3000").build()
        OkHttpClient().newWebSocket(
            request,
            RemoteDataSource.MyWebSocketListener(application.applicationContext)
        )
        CoroutineScope(Dispatchers.Main).launch { collectEvents() }
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

    private suspend fun collectEvents() {
        while (true) {
            val res = JSONObject(RemoteDataSource.eventChannel.receive())
            val song = Gson().fromJson(res.getJSONObject("payload").toString(), Song::class.java)
            Log.d("ws", "received $song")
            refresh()

//            if (res.getString("type") == "updated") {
//                songRepository.update(song)
//            } else songRepository.save(song)
        }
    }
}
