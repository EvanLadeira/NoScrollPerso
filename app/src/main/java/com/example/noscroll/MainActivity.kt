package com.perso.noscroll

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var ui: Ui
    private lateinit var counter: TextView
    private lateinit var quotaValue: TextView
    private lateinit var quotaCaption: TextView
    private lateinit var allowValue: TextView
    private lateinit var quotaInput: EditText
    private lateinit var lockInput: EditText
    private lateinit var allowInput: EditText
    private lateinit var cancelAllowButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = Ui(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ui.dp(20), ui.dp(16), ui.dp(20), ui.dp(32))
        }

        // --- En-tête ---
        content.addView(ui.title("NoScroll"))
        content.addView(ui.soft("Reels, Shorts et TikTok sous contrôle"))
        content.addView(ui.space(20))

        // --- Carte : blocage ---
        val blockCard = ui.card()
        blockCard.addView(ui.switchRow("Blocage actif", Prefs.isEnabled(this)) {
            Prefs.setEnabled(this, it)
        })
        counter = ui.soft("")
        blockCard.addView(counter)
        content.addView(blockCard)

        // --- Carte : quota ---
        val quotaCard = ui.card()
        quotaCard.addView(ui.sectionTitle("Quota"))
        quotaCard.addView(ui.switchRow("Mode quota", Prefs.isQuotaMode(this)) {
            Prefs.setQuotaMode(this, it)
            updateStatus()
        })
        quotaValue = ui.bigValue()
        quotaCaption = ui.soft("")
        quotaCard.addView(quotaValue)
        quotaCard.addView(quotaCaption)

        quotaInput = ui.field(Prefs.quotaMinutes(this))
        lockInput = ui.field(Prefs.lockMinutes(this))
        quotaCard.addView(ui.space(12))
        quotaCard.addView(
            ui.row(
                ui.column("Reels autorisés (min)", quotaInput),
                ui.column("Verrouillage (min)", lockInput)
            )
        )
        quotaCard.addView(ui.space(12))
        quotaCard.addView(ui.button("Enregistrer") { saveQuota() })
        content.addView(quotaCard)

        // --- Carte : autorisation temporaire ---
        val allowCard = ui.card()
        allowCard.addView(ui.sectionTitle("Autorisation temporaire"))
        allowCard.addView(
            ui.soft("Laisse passer les reels un moment, par exemple celui d'un ami. Rien n'est bloqué ni décompté.")
        )
        allowValue = ui.bigValue()
        allowCard.addView(allowValue)
        allowInput = ui.field(Prefs.pauseMinutes(this))
        allowCard.addView(ui.space(12))
        allowCard.addView(ui.column("Durée (min)", allowInput))
        allowCard.addView(ui.space(12))
        allowCard.addView(ui.button("Autoriser") { startAllow() })
        cancelAllowButton = ui.button("Annuler l'autorisation", primary = false) { cancelAllow() }
        allowCard.addView(ui.space(8))
        allowCard.addView(cancelAllowButton)
        content.addView(allowCard)

        // --- Pied de page ---
        content.addView(
            ui.button("Réglages d'accessibilité", primary = false) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )

        val scroll = ScrollView(this).apply {
            setBackgroundColor(ui.bg)
            isFillViewport = true
            clipToPadding = false
            addView(content)
        }

        // Garde le contenu hors de la barre d'état, de la barre de navigation et du clavier
        scroll.setOnApplyWindowInsetsListener { v, insets ->
            val top: Int
            val bottom: Int
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.ime())
                top = bars.top
                bottom = bars.bottom
            } else {
                top = insets.systemWindowInsetTop
                bottom = insets.systemWindowInsetBottom
            }
            v.setPadding(0, top, 0, bottom)
            insets
        }

        setContentView(scroll)
        setupWindow()
    }

    private fun setupWindow() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(if (ui.isNight) 0 else mask, mask)
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresh)
    }

    // --- Actions ---

    private fun saveQuota() {
        val q = readMinutes(quotaInput, 120)
        val l = readMinutes(lockInput, 720)
        if (q == null || l == null) {
            Toast.makeText(this, "Entre des nombres de minutes", Toast.LENGTH_SHORT).show()
            return
        }
        quotaInput.setText(q.toString())
        lockInput.setText(l.toString())
        Prefs.setQuotaSettings(this, q, l)
        Toast.makeText(this, "Quota enregistré", Toast.LENGTH_SHORT).show()
        updateStatus()
    }

    private fun startAllow() {
        val m = readMinutes(allowInput, 240)
        if (m == null) {
            Toast.makeText(this, "Entre un nombre de minutes", Toast.LENGTH_SHORT).show()
            return
        }
        allowInput.setText(m.toString())
        Prefs.startPause(this, m)
        updateStatus()
    }

    private fun cancelAllow() {
        Prefs.cancelPause(this)
        updateStatus()
    }

    private fun readMinutes(field: EditText, max: Int): Int? =
        field.text.toString().toIntOrNull()?.coerceIn(1, max)

    // --- Affichage ---

    private fun fmt(ms: Long): String {
        val total = (ms / 1000).toInt()
        return "%d:%02d".format(total / 60, total % 60)
    }

    private fun updateStatus() {
        counter.text = "Blocages aujourd'hui : ${Prefs.todayCount(this)}"

        when {
            !Prefs.isQuotaMode(this) -> {
                quotaValue.text = "—"
                quotaCaption.text = "Mode quota désactivé : blocage total"
            }
            Prefs.isLocked(this) -> {
                quotaValue.text = fmt(Prefs.lockRemainingMs(this))
                quotaCaption.text = "Verrouillé : les reels reviennent dans ce délai"
            }
            else -> {
                val left = (Prefs.quotaMinutes(this) * 60_000L - Prefs.quotaUsedMs(this))
                    .coerceAtLeast(0L)
                quotaValue.text = fmt(left)
                quotaCaption.text = "de reels restantes avant le verrouillage"
            }
        }

        val remaining = Prefs.pauseRemainingMs(this)
        if (remaining > 0) {
            allowValue.text = fmt(remaining)
            cancelAllowButton.visibility = View.VISIBLE
        } else {
            allowValue.text = "—"
            cancelAllowButton.visibility = View.GONE
        }
    }
}