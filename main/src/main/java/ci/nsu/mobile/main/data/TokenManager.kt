package ci.nsu.mobile.main.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val TOKEN_KEY = "jwt_token"
    private val USER_ID_KEY = "user_id"

    var userId: Long
        get() = prefs.getLong(USER_ID_KEY, -1)
        set(value) {
            prefs.edit().putLong(USER_ID_KEY, value).apply()
        }

    var token: String?
        get() = prefs.getString(TOKEN_KEY, null)
        set(value) {
            if (value == null) {
                prefs.edit().remove(TOKEN_KEY).apply()
            } else {
                prefs.edit().putString(TOKEN_KEY, value).apply()
            }
        }

    fun clearToken() {
        token = null
        userId = -1
    }
}