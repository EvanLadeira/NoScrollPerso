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
    private const val KEY_PAUSE_UNTIL = "pause_until"
    private const val KEY_PAUSE_MIN = "pause_minutes"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun isEnabled(ctx: Context) = sp(ctx).getBoolean(KEY_ENABLED, true)

    fun setEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun todayCount(ctx: Context): Int {
        val p = sp(ctx)
        return if (p.getString(KEY_DAY, null) == today()) p.getInt(KEY_COUNT, 0) else 0
    }

    fun increment(ctx: Context) {
        val n = todayCount(ctx) + 1
        sp(ctx).edit().putString(KEY_DAY, today()).putInt(KEY_COUNT, n).apply()
    }

    // Dernière durée de pause choisie (10 min par défaut)
    fun pauseMinutes(ctx: Context) = sp(ctx).getInt(KEY_PAUSE_MIN, 10)

    fun startPause(ctx: Context, minutes: Int) {
        sp(ctx).edit()
            .putInt(KEY_PAUSE_MIN, minutes)
            .putLong(KEY_PAUSE_UNTIL, System.currentTimeMillis() + minutes * 60_000L)
            .apply()
    }

    fun cancelPause(ctx: Context) {
        sp(ctx).edit().remove(KEY_PAUSE_UNTIL).apply()
    }

    fun pauseRemainingMs(ctx: Context): Long =
        (sp(ctx).getLong(KEY_PAUSE_UNTIL, 0L) - System.currentTimeMillis()).coerceAtLeast(0L)

    fun isPaused(ctx: Context) = pauseRemainingMs(ctx) > 0
}