package ro.ubbcluj.ro.birdie.myapp.todo.song

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.todo.data.Song
import ro.ubbcluj.ro.birdie.myapp.todo.data.SongRepository
import java.text.SimpleDateFormat
import java.util.*

class SongEditViewModel : ViewModel() {
    private val mutableItem = MutableLiveData<Song>().apply { value = Song("", "", 0, "", false) }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val item: LiveData<Song> = mutableItem
    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            Log.i(TAG, "loadItem...")
            mutableFetching.value = true
            mutableException.value = null
            try {
                mutableItem.value = SongRepository.load(itemId)
                Log.i(TAG, "loadItem succeeded")
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadItem failed", e)
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun saveOrUpdateItem(title: String, streams: String, date: String, hasAwards: String) {
        viewModelScope.launch {
            Log.i(TAG, "saveOrUpdateItem...");
            val item = mutableItem.value ?: return@launch
            item.title = title
            item.streams = streams.toInt()
            item.releaseDate = getUnixDateString(date)
            item.hasAwards = hasAwards.toBoolean()
            mutableFetching.value = true
            mutableException.value = null
            try {
                if (item.id.isNotEmpty()) {
                    mutableItem.value = SongRepository.update(item)
                } else {
                    mutableItem.value = SongRepository.save(item)
                }
                Log.i(TAG, "saveOrUpdateItem succeeded");
                mutableCompleted.value = true
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "saveOrUpdateItem failed", e);
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun getUnixDateString(date: String) : String{
        date?.let{
            val date1: Date = SimpleDateFormat("dd.MM.yyyy").parse(date)
            return date1.time.toString()
        }
    }
}
