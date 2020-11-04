package ro.ubbcluj.ro.birdie.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import ro.ubbcluj.ro.birdie.myapp.core.TAG
import ro.ubbcluj.ro.birdie.myapp.todo.data.EchoWebSocketListener
import ro.ubbcluj.ro.birdie.myapp.todo.data.SongRepository

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        Log.i(TAG, "onCreate")

        var client = OkHttpClient()
        val request: Request = Request.Builder().url("ws://192.168.0.104:3000/").build()
        val listener = EchoWebSocketListener(SongRepository)
        client.newWebSocket(request, listener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }
}