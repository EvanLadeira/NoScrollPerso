package com.perso.noscroll

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Prefs {
    private const val FILE = "noscroll_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_COUNT = "count"
    private const val KEY_DAY = "count_day"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun isEnabled(ctx: Context) = sp(ctx).getBoolean(KEY_ENABLED, true)

    fun setEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    // Compteur du jour : repart à 0 automatiquement quand la date change
    fun todayCount(ctx: Context): Int {
        val p = sp(ctx)
        return if (p.getString(KEY_DAY, null) == today()) p.getInt(KEY_COUNT, 0) else 0
    }

    fun increment(ctx: Context) {
        val n = todayCount(ctx) + 1
        sp(ctx).edit().putString(KEY_DAY, today()).putInt(KEY_COUNT, n).apply()
    }
}