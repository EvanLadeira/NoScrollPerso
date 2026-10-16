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
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var counter: TextView
    private lateinit var pauseStatus: TextView
    private lateinit var minutesInput: EditText
    private lateinit var resumeButton: Button

    // Rafraîchit l'écran chaque seconde tant qu'il est visible
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val title = TextView(this).apply {
            text = "NoScroll perso"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val toggle = Switch(this).apply {
            text = "Blocage actif"
            textSize = 18f
            setPadding(0, 48, 0, 48)
            isChecked = Prefs.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                Prefs.setEnabled(this@MainActivity, checked)
            }
        }

        counter = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        minutesInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            hint = "minutes"
            setText(Prefs.pauseMinutes(this@MainActivity).toString())
        }

        val pauseButton = Button(this).apply {
            text = "Mettre en pause (minutes)"
            setOnClickListener {
                val minutes = minutesInput.text.toString().toIntOrNull()?.coerceIn(1, 240)
                if (minutes == null) {
                    Toast.makeText(this@MainActivity, "Entre un nombre de minutes", Toast.LENGTH_SHORT).show()
                } else {
                    minutesInput.setText(minutes.toString())
                    Prefs.startPause(this@MainActivity, minutes)
                    updateStatus()
                }
            }
        }

        pauseStatus = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        resumeButton = Button(this).apply {
            text = "Reprendre maintenant"
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

        layout.addView(title)
        layout.addView(toggle)
        layout.addView(counter)
        layout.addView(minutesInput)
        layout.addView(pauseButton)
        layout.addView(pauseStatus)
        layout.addView(resumeButton)
        layout.addView(accessButton)
        setContentView(layout)
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
        val remaining = Prefs.pauseRemainingMs(this)
        if (remaining > 0) {
            val total = (remaining / 1000).toInt()
            pauseStatus.text = "Pause en cours : %d:%02d restantes".format(total / 60, total % 60)
            resumeButton.visibility = View.VISIBLE
        } else {
            pauseStatus.text = "Aucune pause en cours"
            resumeButton.visibility = View.GONE
        }
    }
}