package com.perso.noscroll

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var counter: TextView

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
            setPadding(0, 64, 0, 64)
            isChecked = Prefs.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                Prefs.setEnabled(this@MainActivity, checked)
            }
        }

        counter = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 64)
        }

        val button = Button(this).apply {
            text = "Réglages d'accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(title)
        layout.addView(toggle)
        layout.addView(counter)
        layout.addView(button)
        setContentView(layout)
    }

    // Se rafraîchit à chaque retour sur l'écran, par exemple après un blocage
    override fun onResume() {
        super.onResume()
        counter.text = "Blocages aujourd'hui : ${Prefs.todayCount(this)}"
    }
}