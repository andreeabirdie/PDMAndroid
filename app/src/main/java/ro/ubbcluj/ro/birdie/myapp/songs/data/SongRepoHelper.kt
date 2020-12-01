package ro.ubbcluj.ro.birdie.myapp.songs.data

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ro.ubbcluj.ro.birdie.myapp.core.Properties
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.remote.SongApi

object SongRepoHelper {
    var songRepository: SongRepository? = null
    private var song: Song? = null
    private var viewLifecycleOwner: LifecycleOwner? = null

    fun setSongRepo(songParam: SongRepository) {
        this.songRepository = songParam
    }

    fun setSong(songParam: Song) {
        this.song = songParam
    }

    fun setViewLifecycleOwner(viewLifecycleOwnerParam: LifecycleOwner) {
        viewLifecycleOwner = viewLifecycleOwnerParam
    }

    fun save() {
        viewLifecycleOwner!!.lifecycleScope.launch {
            saveHelper()
        }
    }

    private suspend fun saveHelper(): Result<Song> {
        try {
            if (Properties.instance.internetActive.value!!) {

                val createdSong = SongApi.service.create(SongDTO(
                    song?.title!!,
                    song?.streams!!,
                    song?.releaseDate!!,
                    song?.hasAwards!!,
                    song?.owner,
                ))

                createdSong!!.upToDate= true
                createdSong!!.action = ""
                songRepository!!.songDao.deleteSong(createdSong.title, createdSong.releaseDate)
                songRepository!!.songDao.insert(createdSong)
                Properties.instance.toastMessage.postValue("Song was saved on the server")
                return Result.Success(createdSong)
            } else {
                Log.d(TAG, "internet still not working...")
                return Result.Error(Exception("internet still not working..."))
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun update() {
        viewLifecycleOwner!!.lifecycleScope.launch {
            updateHelper()
        }
    }

    private suspend fun updateHelper(): Result<Song> {
        try {
            if (Properties.instance.internetActive.value!!) {
                Log.d(TAG, "updateNewVersionHelper")
                song!!.upToDate = true
                song!!.action = ""
                val updatedSong = SongApi.service.update(song!!._id, song!!)
                songRepository!!.songDao.update(updatedSong)
                Properties.instance.toastMessage.postValue("Song was updated on the server")
                return Result.Success(updatedSong)
            } else {
                Log.d(TAG, "internet still not working...")
                return Result.Error(Exception("internet still not working..."))
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    fun delete(){
        viewLifecycleOwner!!.lifecycleScope.launch {
            deleteHelper()
        }
    }

    private suspend fun deleteHelper(): Result<Boolean> {
        try {
            if (Properties.instance.internetActive.value!!) {
                Log.d(TAG, "updateNewVersionHelper")
                val deletedSong = SongApi.service.delete(song!!._id)
                songRepository!!.songDao.delete(song!!._id)
                Properties.instance.toastMessage.postValue("Song was deleted on the server")
                return Result.Success(true)
            } else {
                Log.d(TAG, "internet still not working...")
                return Result.Error(Exception("internet still not working..."))
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
}