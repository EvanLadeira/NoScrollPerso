package com.perso.noscroll

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class NoScrollService : AccessibilityService() {

    companion object {
        private const val TAG = "NoScroll"
        private const val DEBUG = false

        // Apps où seul l'écran Reels / Shorts compte (via des IDs de vues)
        private val TARGETS = mapOf(
            "com.instagram.android" to listOf(
                "com.instagram.android:id/clips_viewer_view_pager",
                "com.instagram.android:id/clips_viewer_root"
            ),
            "com.google.android.youtube" to listOf(
                "com.google.android.youtube:id/reel_recycler",
                "com.google.android.youtube:id/reel_player_page_container"
            )
        )

        // Apps traitées en entier
        private val BLOCKED_APPS = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill"
        )

        private const val COOLDOWN_MS = 1500L
    }

    private var lastBlock = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false
    private var lastTick = 0L

    // Tourne chaque seconde, uniquement tant qu'un contenu court est visible
    private val tick = object : Runnable {
        override fun run() {
            val svc = this@NoScrollService
            val active = Prefs.isEnabled(svc) && !Prefs.isPaused(svc) &&
                    Prefs.isQuotaMode(svc) && !Prefs.isLocked(svc)
            val target = if (active) visibleShortPkg() else null
            if (target == null) {
                ticking = false
                return
            }

            val now = System.currentTimeMillis()
            val delta = (now - lastTick).coerceIn(0L, 2000L)
            lastTick = now
            Prefs.addQuotaUsed(svc, delta)

            if (Prefs.quotaUsedMs(svc) >= Prefs.quotaMinutes(svc) * 60_000L) {
                Prefs.startLock(svc)
                ticking = false
                block(target, force = true)
            } else {
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in BLOCKED_APPS && pkg !in TARGETS) return
        if (!Prefs.isEnabled(this) || Prefs.isPaused(this)) return

        val target = if (pkg in BLOCKED_APPS) pkg else (visibleShortPkg() ?: return)

        when {
            // Mode normal, ou verrouillage en cours : on bloque tout de suite
            !Prefs.isQuotaMode(this) || Prefs.isLocked(this) -> block(target)
            // Mode quota avec du temps restant : on laisse passer et on compte
            else -> startTicking()
        }
    }

    private fun startTicking() {
        if (ticking) return
        ticking = true
        lastTick = System.currentTimeMillis()
        handler.postDelayed(tick, 1000)
    }

    // Renvoie le paquet de l'app si un Reel / Short / TikTok est à l'écran, sinon null
    private fun visibleShortPkg(): String? {
        val root = rootInActiveWindow ?: return null
        val pkg = root.packageName?.toString() ?: return null
        if (pkg in BLOCKED_APPS) return pkg
        val ids = TARGETS[pkg] ?: return null
        if (DEBUG) dumpIds(root)
        val visible = ids.any { id ->
            root.findAccessibilityNodeInfosByViewId(id).any { it.isVisibleToUser }
        }
        return if (visible) pkg else null
    }

    private fun block(pkg: String, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastBlock <= COOLDOWN_MS) return
        lastBlock = now
        Prefs.increment(this)

        performGlobalAction(
            if (pkg in BLOCKED_APPS) GLOBAL_ACTION_HOME else GLOBAL_ACTION_BACK
        )

        val message = when {
            Prefs.isQuotaMode(this) && Prefs.isLocked(this) ->
                (if (force) "Quota atteint. " else "") +
                        "Verrouillé : ${(Prefs.lockRemainingMs(this) + 59_999) / 60_000} min restantes"
            pkg in BLOCKED_APPS -> "TikTok bloqué"
            else -> "Reels / Shorts bloqués"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dumpIds(node: AccessibilityNodeInfo?) {
        node ?: return
        node.viewIdResourceName?.let { Log.d(TAG, it) }
        for (i in 0 until node.childCount) dumpIds(node.getChild(i))
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }
}