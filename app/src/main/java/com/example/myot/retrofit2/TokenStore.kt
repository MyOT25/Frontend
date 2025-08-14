package com.example.myot.retrofit2

import android.content.Context

object TokenStore {
    private const val PREF = "auth_pref"
    private const val KEY_ACCESS = "access_token"

    fun saveAccessToken(ctx: Context, token: String?) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACCESS, token).apply()
    }

    fun loadAccessToken(ctx: Context): String? {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS, null)
    }

    fun clear(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().remove(KEY_ACCESS).apply()
    }
}