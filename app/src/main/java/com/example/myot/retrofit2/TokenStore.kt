package com.example.myot.retrofit2

import android.content.Context

object TokenStore {
    private const val PREF = "token_store"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_USER_ID = "user_id"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun saveAccessToken(ctx: Context, token: String) {
        prefs(ctx).edit().putString(KEY_ACCESS, token).apply()
    }
    fun loadAccessToken(ctx: Context): String? =
        prefs(ctx).getString(KEY_ACCESS, null)

    fun saveUserId(ctx: Context, userId: Long) {
        prefs(ctx).edit().putLong(KEY_USER_ID, userId).apply()
    }
    fun loadUserId(ctx: Context): Long? {
        val v = prefs(ctx).getLong(KEY_USER_ID, -1L)
        return if (v > 0) v else null
    }
}