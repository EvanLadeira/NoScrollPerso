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

        // Package -> IDs présents UNIQUEMENT sur l'écran Reels / Shorts.
        // Ne mets jamais l'ID d'un bouton de la barre de navigation :
        // il existe partout et tu bloquerais toute l'app.
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

        private const val COOLDOWN_MS = 1500L
    }

    private var lastBlock = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val ids = TARGETS[pkg] ?: return
        if (!Prefs.isEnabled(this)) return

        val root = rootInActiveWindow ?: return
        if (DEBUG) dumpIds(root)

        val onShortVideo = ids.any { id ->
            root.findAccessibilityNodeInfosByViewId(id).any { it.isVisibleToUser }
        }

        val now = System.currentTimeMillis()
        if (onShortVideo && now - lastBlock > COOLDOWN_MS) {
            lastBlock = now
            Prefs.increment(this)
            performGlobalAction(GLOBAL_ACTION_BACK)
            Toast.makeText(this, "Reels / Shorts bloqués", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dumpIds(node: AccessibilityNodeInfo?) {
        node ?: return
        node.viewIdResourceName?.let { Log.d(TAG, it) }
        for (i in 0 until node.childCount) dumpIds(node.getChild(i))
    }

    override fun onInterrupt() {}
}