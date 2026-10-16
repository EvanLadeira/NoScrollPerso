package com.perso.noscroll

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var counter: TextView
    private lateinit var quotaStatus: TextView
    private lateinit var allowStatus: TextView
    private lateinit var allowInput: EditText
    private lateinit var quotaInput: EditText
    private lateinit var lockInput: EditText
    private lateinit var cancelAllowButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    private fun label(content: String, size: Float = 16f) = TextView(this).apply {
        text = content
        textSize = size
        gravity = Gravity.CENTER
        setPadding(0, 24, 0, 24)
    }

    private fun numberField(value: Int, hintText: String) = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        gravity = Gravity.CENTER
        hint = hintText
        setText(value.toString())
    }

    private fun readMinutes(field: EditText, max: Int): Int? =
        field.text.toString().toIntOrNull()?.coerceIn(1, max)

    private fun fmt(ms: Long): String {
        val total = (ms / 1000).toInt()
        return "%d:%02d".format(total / 60, total % 60)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val enabledSwitch = Switch(this).apply {
            text = "Blocage actif"
            textSize = 18f
            setPadding(0, 32, 0, 32)
            isChecked = Prefs.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                Prefs.setEnabled(this@MainActivity, checked)
            }
        }

        counter = label("")

        // --- Quota ---
        val quotaSwitch = Switch(this).apply {
            text = "Mode quota"
            textSize = 18f
            setPadding(0, 32, 0, 32)
            isChecked = Prefs.isQuotaMode(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                Prefs.setQuotaMode(this@MainActivity, checked)
                updateStatus()
            }
        }

        quotaInput = numberField(Prefs.quotaMinutes(this), "minutes")
        lockInput = numberField(Prefs.lockMinutes(this), "minutes")

        val saveButton = Button(this).apply {
            text = "Enregistrer le quota"
            setOnClickListener {
                val q = readMinutes(quotaInput, 120)
                val l = readMinutes(lockInput, 720)
                if (q == null || l == null) {
                    Toast.makeText(this@MainActivity, "Entre des nombres de minutes", Toast.LENGTH_SHORT).show()
                } else {
                    quotaInput.setText(q.toString())
                    lockInput.setText(l.toString())
                    Prefs.setQuotaSettings(this@MainActivity, q, l)
                    Toast.makeText(this@MainActivity, "Quota enregistré", Toast.LENGTH_SHORT).show()
                    updateStatus()
                }
            }
        }

        quotaStatus = label("")

        // --- Autorisation temporaire ---
        allowInput = numberField(Prefs.pauseMinutes(this), "minutes")

        val allowButton = Button(this).apply {
            text = "Autoriser (minutes)"
            setOnClickListener {
                val m = readMinutes(allowInput, 240)
                if (m == null) {
                    Toast.makeText(this@MainActivity, "Entre un nombre de minutes", Toast.LENGTH_SHORT).show()
                } else {
                    allowInput.setText(m.toString())
                    Prefs.startPause(this@MainActivity, m)
                    updateStatus()
                }
            }
        }

        allowStatus = label("")

        cancelAllowButton = Button(this).apply {
            text = "Annuler l'autorisation"
            setOnClickListener {
                Prefs.cancelPause(this@MainActivity)
                updateStatus()
            }
        }

        val accessButton = Button(this).apply {
            text = "Réglages d'accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(label("NoScroll perso", 22f))
        layout.addView(enabledSwitch)
        layout.addView(counter)
        layout.addView(quotaSwitch)
        layout.addView(label("Reels autorisés (minutes)"))
        layout.addView(quotaInput)
        layout.addView(label("Verrouillage ensuite (minutes)"))
        layout.addView(lockInput)
        layout.addView(saveButton)
        layout.addView(quotaStatus)
        layout.addView(label("Autoriser temporairement (reels d'un ami, etc.)"))
        layout.addView(allowInput)
        layout.addView(allowButton)
        layout.addView(allowStatus)
        layout.addView(cancelAllowButton)
        layout.addView(accessButton)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(layout)
        }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        handler.post(refresh)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refresh)
    }

    private fun updateStatus() {
        counter.text = "Blocages aujourd'hui : ${Prefs.todayCount(this)}"

        quotaStatus.text = when {
            !Prefs.isQuotaMode(this) -> "Mode quota désactivé : blocage total"
            Prefs.isLocked(this) -> "Verrouillé : ${fmt(Prefs.lockRemainingMs(this))} restantes"
            else -> "Quota : ${fmt(Prefs.quotaUsedMs(this))} / ${fmt(Prefs.quotaMinutes(this) * 60_000L)}"
        }

        val remaining = Prefs.pauseRemainingMs(this)
        if (remaining > 0) {
            allowStatus.text = "Autorisé : ${fmt(remaining)} restantes"
            cancelAllowButton.visibility = View.VISIBLE
        } else {
            allowStatus.text = "Aucune autorisation en cours"
            cancelAllowButton.visibility = View.GONE
        }
    }
}