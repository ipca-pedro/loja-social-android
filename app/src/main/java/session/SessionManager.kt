package session
import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences
    private val editor: SharedPreferences.Editor

    companion object {
        private const val PREFS_NAME = "LojaSocialPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    init {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = prefs.edit()
    }

    fun saveAuthToken(token: String) {
        editor.putString(KEY_AUTH_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN)
        editor.apply()
    }
}