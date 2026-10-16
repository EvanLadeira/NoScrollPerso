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
    private const val KEY_QUOTA_MODE = "quota_mode"
    private const val KEY_QUOTA_MIN = "quota_minutes"
    private const val KEY_LOCK_MIN = "lock_minutes"
    private const val KEY_QUOTA_USED = "quota_used_ms"
    private const val KEY_QUOTA_DAY = "quota_day"
    private const val KEY_LOCK_UNTIL = "lock_until"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun now() = System.currentTimeMillis()

    // --- Interrupteur général ---
    fun isEnabled(ctx: Context) = sp(ctx).getBoolean(KEY_ENABLED, true)

    fun setEnabled(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    // --- Compteur de blocages du jour ---
    fun todayCount(ctx: Context): Int {
        val p = sp(ctx)
        return if (p.getString(KEY_DAY, null) == today()) p.getInt(KEY_COUNT, 0) else 0
    }

    fun increment(ctx: Context) {
        val n = todayCount(ctx) + 1
        sp(ctx).edit().putString(KEY_DAY, today()).putInt(KEY_COUNT, n).apply()
    }

    // --- Autorisation temporaire (l'ancienne "pause" de la V3) ---
    fun pauseMinutes(ctx: Context) = sp(ctx).getInt(KEY_PAUSE_MIN, 10)

    fun startPause(ctx: Context, minutes: Int) {
        sp(ctx).edit()
            .putInt(KEY_PAUSE_MIN, minutes)
            .putLong(KEY_PAUSE_UNTIL, now() + minutes * 60_000L)
            .apply()
    }

    fun cancelPause(ctx: Context) {
        sp(ctx).edit().remove(KEY_PAUSE_UNTIL).apply()
    }

    fun pauseRemainingMs(ctx: Context): Long =
        (sp(ctx).getLong(KEY_PAUSE_UNTIL, 0L) - now()).coerceAtLeast(0L)

    fun isPaused(ctx: Context) = pauseRemainingMs(ctx) > 0

    // --- Mode quota ---
    fun isQuotaMode(ctx: Context) = sp(ctx).getBoolean(KEY_QUOTA_MODE, false)

    fun setQuotaMode(ctx: Context, value: Boolean) {
        sp(ctx).edit().putBoolean(KEY_QUOTA_MODE, value).apply()
    }

    fun quotaMinutes(ctx: Context) = sp(ctx).getInt(KEY_QUOTA_MIN, 5)
    fun lockMinutes(ctx: Context) = sp(ctx).getInt(KEY_LOCK_MIN, 30)

    fun setQuotaSettings(ctx: Context, quotaMin: Int, lockMin: Int) {
        sp(ctx).edit().putInt(KEY_QUOTA_MIN, quotaMin).putInt(KEY_LOCK_MIN, lockMin).apply()
    }

    // Temps de reels déjà consommé (repart à 0 chaque jour)
    fun quotaUsedMs(ctx: Context): Long {
        val p = sp(ctx)
        return if (p.getString(KEY_QUOTA_DAY, null) == today()) p.getLong(KEY_QUOTA_USED, 0L) else 0L
    }

    fun addQuotaUsed(ctx: Context, deltaMs: Long) {
        sp(ctx).edit()
            .putString(KEY_QUOTA_DAY, today())
            .putLong(KEY_QUOTA_USED, quotaUsedMs(ctx) + deltaMs)
            .apply()
    }

    // Quota épuisé : on verrouille, et le compteur repart à 0 pour la fin du verrouillage
    fun startLock(ctx: Context) {
        sp(ctx).edit()
            .putLong(KEY_LOCK_UNTIL, now() + lockMinutes(ctx) * 60_000L)
            .putString(KEY_QUOTA_DAY, today())
            .putLong(KEY_QUOTA_USED, 0L)
            .apply()
    }

    fun lockRemainingMs(ctx: Context): Long =
        (sp(ctx).getLong(KEY_LOCK_UNTIL, 0L) - now()).coerceAtLeast(0L)

    fun isLocked(ctx: Context) = lockRemainingMs(ctx) > 0
}