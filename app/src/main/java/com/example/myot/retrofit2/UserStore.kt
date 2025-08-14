package com.example.myot.retrofit2

import android.content.Context

object UserStore {
    private const val PREF = "user_pref"
    private const val KEY_USER_ID = "user_id"

    fun saveUserId(ctx: Context, id: Long) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putLong(KEY_USER_ID, id).apply()

    fun loadUserId(ctx: Context): Long? =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L).takeIf { it > 0 }
}