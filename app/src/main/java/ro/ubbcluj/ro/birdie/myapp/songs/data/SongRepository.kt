package ro.ubbcluj.ro.birdie.myapp.songs.data

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.Properties
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.local.SongDao
import ro.ubbcluj.ro.birdie.myapp.songs.data.remote.SongApi

class SongRepository(val songDao: SongDao) {

    var songs = MediatorLiveData<List<Song>>().apply { postValue(emptyList()) }

    suspend fun refresh(): Result<Boolean> {
        try {
            if (Properties.instance.internetActive.value!!) {
                val songsApi = SongApi.service.find()
                songs.value = songsApi
                for (song in songsApi) {
                    song.owner = AuthRepository.getUsername()
                    songDao.insert(song)
                }
            } else
                songs.addSource(songDao.getAll(AuthRepository.getUsername())) {
                    songs.value = it
                }
            return Result.Success(true)
        } catch (e: Exception) {
            songs.addSource(songDao.getAll(AuthRepository.getUsername())) {
                songs.value = it
            }
            return Result.Error(e)
        }
    }

    fun getById(songId: String): LiveData<Song> {
        return songDao.getById(songId)
    }

    suspend fun save(song: Song): Result<Song> {
        try {
            if (Properties.instance.internetActive.value!!) {
                val createdSong = SongApi.service.create(
                    SongDTO(
                        song.title,
                        song.streams,
                        song.releaseDate,
                        song.hasAwards,
                        song.owner,
                    )
                )
                createdSong.upToDate = true
                createdSong.action = ""
                songDao.insert(createdSong)
                return Result.Success(createdSong)
            } else {
                song.upToDate = false
                song.action = "save"
                songDao.insert(song)
                Properties.instance.toastMessage.postValue("Song was saved locally. It will be sent to the server once you connect to the internet")
                return Result.Success(song)
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(song: Song): Result<Song> {
        try {
            if (Properties.instance.internetActive.value!!) {
                val updatedSong = SongApi.service.update(song._id, song)
                updatedSong.upToDate = true
                updatedSong.action = ""
                songDao.update(updatedSong)
                return Result.Success(updatedSong)
            }
            else {
                song.upToDate = false
                song.action = "update"
                songDao.update(song)
                Properties.instance.toastMessage.postValue("Song was updated locally. It will be sent to the server once you connect to the internet")
                return Result.Success(song)
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun delete(song: Song): Result<Boolean> {
        try {
            if (Properties.instance.internetActive.value!!) {

                SongApi.service.delete(song._id)
                songDao.delete(song._id)
                return Result.Success(true)
            }
            else{
                song.upToDate = false
                song.action = "delete"
                songDao.update(song)
                Properties.instance.toastMessage.postValue("Song was deleted locally. It will be sent to the server once you connect to the internet")
                return Result.Success(true)
            }
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
}