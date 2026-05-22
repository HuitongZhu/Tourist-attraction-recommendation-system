package com.example.travel

import android.content.Context

object UserSession {
    private const val PREFS_NAME = "travel_user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_TYPE = "user_type"

    fun save(context: Context, userId: String, userName: String?, userType: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_TYPE, userType)
            .apply()
        NetworkClient.userId = userId
        NetworkClient.userName = userName
        NetworkClient.userType = userType
    }

    fun restore(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        NetworkClient.userId = prefs.getString(KEY_USER_ID, null)
        NetworkClient.userName = prefs.getString(KEY_USER_NAME, null)
        NetworkClient.userType = prefs.getString(KEY_USER_TYPE, null)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        NetworkClient.userId = null
        NetworkClient.userName = null
        NetworkClient.userType = null
    }

    fun isLoggedIn(): Boolean = !NetworkClient.userId.isNullOrBlank()
}
