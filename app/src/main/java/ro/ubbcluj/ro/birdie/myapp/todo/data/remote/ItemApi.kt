package ro.ubbcluj.ro.birdie.myapp.todo.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ro.ubbcluj.ro.birdie.myapp.todo.data.Song

object ItemApi {
    private const val URL = "http://192.168.0.104:3000/"

    interface Service {
        @GET("/song")
        suspend fun find(): List<Song>

        @GET("/song/{id}")
        suspend fun read(@Path("id") itemId: String): Song;

        @Headers("Content-Type: application/json")
        @POST("/song")
        suspend fun create(@Body item: Song): Song

        @Headers("Content-Type: application/json")
        @PUT("/song/{id}")
        suspend fun update(@Path("id") itemId: String, @Body item: Song): Song
    }

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()

    val service: Service = retrofit.create(Service::class.java)
}