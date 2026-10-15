package com.perso.noscroll

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val label = TextView(this).apply {
            text = "NoScroll perso\n\nActive le service dans Accessibilité > Applis installées > NoScrollPerso."
            textSize = 16f
            gravity = Gravity.CENTER
        }

        val button = Button(this).apply {
            text = "Ouvrir les réglages d'accessibilité"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        layout.addView(label)
        layout.addView(button)
        setContentView(layout)
    }
}