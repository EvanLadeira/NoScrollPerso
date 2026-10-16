package com.perso.noscroll

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class PauseTileService : TileService() {

    // Appelé à chaque ouverture du panneau de réglages rapides
    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        when {
            !Prefs.isEnabled(this) -> Prefs.setEnabled(this, true)
            Prefs.isPaused(this) -> Prefs.cancelPause(this)
            else -> Prefs.startPause(this, Prefs.pauseMinutes(this))
        }
        refreshTile()
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val remaining = Prefs.pauseRemainingMs(this)

        val newState: Int
        val sub: String
        when {
            !Prefs.isEnabled(this) -> {
                newState = Tile.STATE_INACTIVE
                sub = "Désactivé"
            }
            remaining > 0 -> {
                newState = Tile.STATE_INACTIVE
                sub = "Pause : ${(remaining + 59_999) / 60_000} min"
            }
            else -> {
                newState = Tile.STATE_ACTIVE
                sub = "Actif"
            }
        }

        tile.label = "NoScroll"
        tile.state = newState
        if (Build.VERSION.SDK_INT >= 29) tile.subtitle = sub
        tile.updateTile()
    }
}