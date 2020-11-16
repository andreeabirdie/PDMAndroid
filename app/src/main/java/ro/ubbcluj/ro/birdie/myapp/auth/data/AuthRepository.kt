package ro.ubbcluj.ro.birdie.myapp.auth.data

import android.content.Context
import android.content.SharedPreferences
import ro.ubbcluj.ro.birdie.myapp.core.Api
import ro.ubbcluj.ro.birdie.myapp.core.Result
import ro.ubbcluj.ro.birdie.myapp.auth.data.remote.RemoteAuthDataSource

object AuthRepository {

    var prefs: SharedPreferences? = null

    var user: User? = null
        private set

    init {
        user = null
    }

    fun logout() {
        user = null
        Api.tokenInterceptor.token = null
    }

    suspend fun login(username: String, password: String, context: Context): Result<TokenHolder> {
        val user = User(username, password)
        val result = RemoteAuthDataSource.login(user)
        if (result is Result.Success<TokenHolder>) {
            setLoggedInUser(user, result.data, context)
        }

        if (prefs == null)
            prefs = context.getSharedPreferences("ro.ubbcluj.ro.birdie.myapp", Context.MODE_PRIVATE)
        val editor = prefs!!.edit()
        editor.putString("username", username)
        editor.apply()

        return result
    }

    private fun setLoggedInUser(user: User, tokenHolder: TokenHolder, context: Context) {
        AuthRepository.user = user
        Api.tokenInterceptor.token = tokenHolder.token

        if (prefs == null)
            prefs = context.getSharedPreferences("ro.ubbcluj.ro.birdie.myapp", Context.MODE_PRIVATE)
        val editor = prefs!!.edit()
        editor.putString("token", tokenHolder.token)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        if (prefs == null)
            prefs = context.getSharedPreferences("ro.ubbcluj.ro.birdie.myapp", Context.MODE_PRIVATE)
        val token = prefs!!.getString("token", "")!!
        if (token != "")
            Api.tokenInterceptor.token = token

        return token != ""
    }

    fun getUsername(): String = prefs!!.getString("username", "")!!
}
