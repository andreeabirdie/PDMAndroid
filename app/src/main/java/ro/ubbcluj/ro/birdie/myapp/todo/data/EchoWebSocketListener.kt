package ro.ubbcluj.ro.birdie.myapp.todo.data

import android.util.Log
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.decodeHex

class EchoWebSocketListener(
    val repo: SongRepository
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("Hello, it's Saurel !")
        webSocket.send("What's up ?")
        webSocket.send("deadbeef".decodeHex())
//        webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        output("Receiving : " + text)
        val myText = text.subSequence(37, text.length - 2).toString()
        Log.d("wss", "fromJson $myText")
        repo.saveLocally(Gson().fromJson(myText, Song::class.java))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        output("Error : " + t.message)
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    private fun output(txt: String) {
        Log.d("WSS", txt)
    }
}