package com.perso.noscroll

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView

// Palette grise (clair / sombre) et petits composants, sans bibliothèque externe
class Ui(private val ctx: Context) {

    val isNight = (ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun c(light: Long, dark: Long): Int = (if (isNight) dark else light).toInt()

    // --- Palette : modifie les teintes ici ---
    val bg = c(0xFFF1F1F1, 0xFF121212)
    val cardBg = c(0xFFFFFFFF, 0xFF1E1E1E)
    val stroke = c(0xFFDEDEDE, 0xFF2E2E2E)
    val strokeStrong = c(0xFFC4C4C4, 0xFF4A4A4A)
    val textMain = c(0xFF1C1C1C, 0xFFEDEDED)
    val textSoft = c(0xFF6B6B6B, 0xFF9E9E9E)
    val accent = c(0xFF2B2B2B, 0xFFE6E6E6)
    val onAccent = c(0xFFFFFFFF, 0xFF121212)
    val fieldBg = c(0xFFF1F1F1, 0xFF2A2A2A)
    private val trackOn = c(0xFF8A8A8A, 0xFF6E6E6E)
    private val thumbOff = c(0xFFFFFFFF, 0xFF8A8A8A)
    private val trackOff = c(0xFFBDBDBD, 0xFF3A3A3A)

    fun dp(v: Int): Int = (v * ctx.resources.displayMetrics.density).toInt()

    fun shape(color: Int, radiusDp: Int, strokeColor: Int? = null) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        if (strokeColor != null) setStroke(dp(1), strokeColor)
    }

    // --- Textes ---
    fun title(content: String) = TextView(ctx).apply {
        text = content
        textSize = 30f
        setTextColor(textMain)
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        setPadding(0, dp(8), 0, 0)
    }

    fun soft(content: String) = TextView(ctx).apply {
        text = content
        textSize = 14f
        setTextColor(textSoft)
        setPadding(0, dp(4), 0, dp(4))
    }

    fun sectionTitle(content: String) = TextView(ctx).apply {
        text = content
        textSize = 12f
        isAllCaps = true
        letterSpacing = 0.1f
        setTextColor(textSoft)
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        setPadding(0, 0, 0, dp(8))
    }

    fun bigValue() = TextView(ctx).apply {
        textSize = 40f
        setTextColor(textMain)
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        setPadding(0, dp(12), 0, 0)
    }

    // --- Conteneurs ---
    fun card() = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        background = shape(cardBg, 20, stroke)
        setPadding(dp(20), dp(20), dp(20), dp(20))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(16) }
    }

    fun space(heightDp: Int) = View(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(heightDp)
        )
    }

    // Un petit libellé au-dessus d'un champ
    fun column(caption: String, input: View) = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        addView(soft(caption))
        addView(input)
    }

    // Deux colonnes côte à côte, de largeur égale
    fun row(first: View, second: View) = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        first.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        second.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginStart = dp(12) }
        addView(first)
        addView(second)
    }

    // --- Contrôles ---
    fun field(value: Int) = EditText(ctx).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        gravity = Gravity.CENTER
        textSize = 20f
        setTextColor(textMain)
        setHintTextColor(textSoft)
        background = shape(fieldBg, 12)
        setPadding(dp(12), dp(14), dp(12), dp(14))
        setText(value.toString())
    }

    fun button(content: String, primary: Boolean = true, onClick: () -> Unit) = Button(ctx).apply {
        text = content
        isAllCaps = false
        textSize = 15f
        stateListAnimator = null
        minHeight = dp(52)
        minimumHeight = dp(52)
        setTextColor(if (primary) onAccent else textMain)
        val base = if (primary) shape(accent, 14) else shape(Color.TRANSPARENT, 14, strokeStrong)
        background = RippleDrawable(ColorStateList.valueOf(0x33808080), base, null)
        setOnClickListener { onClick() }
    }

    fun switchRow(content: String, initial: Boolean, onChange: (Boolean) -> Unit): LinearLayout {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val label = TextView(ctx).apply {
            text = content
            textSize = 17f
            setTextColor(textMain)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val toggle = Switch(ctx).apply {
            isChecked = initial
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            thumbTintList = ColorStateList(states, intArrayOf(accent, thumbOff))
            trackTintList = ColorStateList(states, intArrayOf(trackOn, trackOff))
            setOnCheckedChangeListener { _, isOn -> onChange(isOn) }
        }
        row.addView(label)
        row.addView(toggle)
        return row
    }
}