package com.perso.noscroll

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class NoScrollService : AccessibilityService() {

    companion object {
        private const val TAG = "NoScroll"

        // true = affiche dans Logcat les IDs de vues de l'écran courant
        private const val DEBUG = false

        // Apps où seul l'écran Reels / Shorts est bloqué (via des IDs de vues)
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

        // Apps bloquées en entier : aucun ID à chercher, retour à l'accueil
        private val BLOCKED_APPS = setOf(
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill"
        )

        private const val COOLDOWN_MS = 1500L
    }

    private var lastBlock = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val wholeApp = pkg in BLOCKED_APPS
        val ids = TARGETS[pkg] ?: emptyList()
        if (!wholeApp && ids.isEmpty()) return
        if (!Prefs.isEnabled(this) || Prefs.isPaused(this)) return

        val shouldBlock = if (wholeApp) {
            true
        } else {
            val root = rootInActiveWindow ?: return
            if (DEBUG) dumpIds(root)
            ids.any { id ->
                root.findAccessibilityNodeInfosByViewId(id).any { it.isVisibleToUser }
            }
        }

        val now = System.currentTimeMillis()
        if (shouldBlock && now - lastBlock > COOLDOWN_MS) {
            lastBlock = now
            Prefs.increment(this)
            if (wholeApp) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                Toast.makeText(this, "TikTok bloqué", Toast.LENGTH_SHORT).show()
            } else {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this, "Reels / Shorts bloqués", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dumpIds(node: AccessibilityNodeInfo?) {
        node ?: return
        node.viewIdResourceName?.let { Log.d(TAG, it) }
        for (i in 0 until node.childCount) dumpIds(node.getChild(i))
    }

    override fun onInterrupt() {}
}