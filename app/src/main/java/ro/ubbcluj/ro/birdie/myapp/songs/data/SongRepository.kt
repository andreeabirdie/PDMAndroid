package ro.ubbcluj.ro.birdie.myapp.songs.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ro.ubbcluj.ro.birdie.myapp.auth.data.AuthRepository
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.songs.data.local.SongDao
import ro.ubbcluj.ro.birdie.myapp.songs.data.remote.SongApi

class SongRepository(val songDao: SongDao) {

    var songs = MediatorLiveData<List<Song>>().apply { postValue(emptyList()) }

    suspend fun refresh(): Result<Boolean> {
        try {
            val songsApi = SongApi.service.find()
            songs.value = songsApi
            for (song in songsApi) {
                song.owner = AuthRepository.getUsername()
                songDao.insert(song)
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
            val createdSong = SongApi.service.create(SongDTO(song.title, song.streams, song.releaseDate, song.hasAwards, song.owner))
            songDao.insert(createdSong)
            return Result.Success(createdSong)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun update(song: Song): Result<Song> {
        try {
            val updatedSong = SongApi.service.update(song._id, song)
            songDao.update(updatedSong)
            return Result.Success(updatedSong)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun delete(songId: String): Result<Boolean> {
        try {
            SongApi.service.delete(songId)
            songDao.delete(songId)
            return Result.Success(true)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }
}