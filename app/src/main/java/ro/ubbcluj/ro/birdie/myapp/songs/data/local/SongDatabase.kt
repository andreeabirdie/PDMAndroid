package ro.ubbcluj.ro.birdie.myapp.songs.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song

@Database(entities = [Song::class], version = 9)
abstract class SongDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SongDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SongDatabase {
            val inst = INSTANCE
            if (inst != null) {
                return inst
            }
            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java,
                    "songs_db"
                )
                    .addCallback(SongDatabaseCallback(scope))
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            INSTANCE = instance
            return instance
        }

        private class SongDatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.songDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(songDao: SongDao) {
        }
    }

}