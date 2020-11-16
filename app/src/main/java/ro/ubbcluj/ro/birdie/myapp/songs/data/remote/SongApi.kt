package ro.ubbcluj.ro.birdie.myapp.songs.data.remote

import retrofit2.Response
import retrofit2.http.*
import ro.ubbcluj.ro.birdie.myapp.core.Api
import ro.ubbcluj.ro.birdie.myapp.songs.data.Song
import ro.ubbcluj.ro.birdie.myapp.songs.data.SongDTO

object SongApi {
    interface Service {
        @GET("/api/song")
        suspend fun find(): List<Song>

        @GET("/api/song/{id}")
        suspend fun read(@Path("id") songId: String): Song;

        @Headers("Content-Type: application/json")
        @POST("/api/song")
        suspend fun create(@Body song: SongDTO): Song

        @Headers("Content-Type: application/json")
        @PUT("/api/song/{id}")
        suspend fun update(@Path("id") songId: String, @Body song: Song): Song

        @DELETE("/api/song/{id}")
        suspend fun delete(@Path("id") songId: String): Response<Unit>
    }

    val service: Service = Api.retrofit.create(Service::class.java)
}