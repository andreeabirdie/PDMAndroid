package ro.ubbcluj.ro.birdie.myapp.todo.data

import android.util.Log
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.todo.data.remote.ItemApi
import java.text.SimpleDateFormat

object SongRepository {
    private var cachedItems: MutableList<Song>? = null;
    private lateinit var functionHolder: FunctionHolder

    fun setFunctionHolder(functionHolder: FunctionHolder) {
        this.functionHolder = functionHolder
    }

    suspend fun loadAll(): List<Song> {
        Log.i(TAG, "loadAll")
        if (cachedItems != null) {
            return cachedItems as List<Song>;
        }
        cachedItems = mutableListOf()
        val items = ItemApi.service.find()
        cachedItems?.addAll(items)
        return cachedItems as List<Song>
    }

    suspend fun load(itemId: String): Song {
        Log.i(TAG, "load")
        val item = cachedItems?.find { it.id == itemId }
        if (item != null) {
            return item
        }
        return ItemApi.service.read(itemId)
    }

    suspend fun save(item: Song): Song {
        Log.i(TAG, "save")
        cachedItems?.add(item)
        return item
    }

    fun saveLocally(song: Song): Song {
        Log.i(TAG, "save")
        cachedItems?.add(song)
        functionHolder.function()
        return song
    }

    suspend fun update(item: Song): Song {
        Log.i(TAG, "update")
        val updatedItem = ItemApi.service.update(item.id, item)
        val index = cachedItems?.indexOfFirst { it.id == item.id }
        if (index != null) {
            cachedItems?.set(index, updatedItem)
        }
        return updatedItem
    }
}