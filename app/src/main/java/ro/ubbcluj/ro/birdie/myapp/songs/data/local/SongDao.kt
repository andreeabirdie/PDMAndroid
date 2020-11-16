package ro.ubbcluj.ro.birdie.myapp.songs.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song

@Dao
interface SongDao {
    @Query("SELECT * from songs WHERE owner=:username ORDER BY title ASC")
    fun getAll(username: String): LiveData<List<Song>>

    @Query("SELECT * FROM Songs WHERE _id=:id ")
    fun getById(id: String): LiveData<Song>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(song: Song)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Query("DELETE FROM songs WHERE _id=:id ")
    suspend fun delete(id: String)
}