package ro.ubbcluj.ro.birdie.myapp.songs.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okio.ByteString

object RemoteDataSource {
    val eventChannel = Channel<String>()

    class MyWebSocketListener(val context: Context) : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "onOpen")
            val prefs = context.getSharedPreferences("ro.ubbcluj.ro.birdie.myapp", Context.MODE_PRIVATE)
            val token = prefs.getString("token", "")
            webSocket.send("{\"type\":\"authorization\",\"payload\":{\"token\":\"$token\"}}")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "onMessage$text")
            runBlocking { eventChannel.send(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "onMessage bytes")
            output("Receiving bytes : " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "onFailure", t)
            t.printStackTrace()
        }

        private fun output(txt: String) {
            Log.d("WebSocket", txt)
        }
    }
}
