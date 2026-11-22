
package com.tv.soundguard

import android.app.Activity
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private lateinit var status: TextView
    private lateinit var controls: LinearLayout
    private lateinit var bassSeek: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(Color.rgb(13, 19, 32))
        createInterface()
        connectAudio()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun createInterface() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(24))
        }

        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "TV SOUND GUARD"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        })

        root.addView(TextView(this).apply {
            text = "Smart Audio Control"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        })

        status = TextView(this).apply {
            text = "Checking audio support..."
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(15), dp(12), dp(15))
            setBackgroundColor(Color.rgb(35, 48, 69))
        }

        root.addView(status)

        root.addView(section("SOUND PROFILES"))

        val profiles = LinearLayout(this)
        profiles.orientation = LinearLayout.HORIZONTAL

        listOf("Normal", "Speech", "Movie", "Night").forEach { name ->
            val button = Button(this)
            button.text = name
            button.isAllCaps = false
            button.setOnClickListener {
                applyProfile(name)
            }

            profiles.addView(
                button,
                LinearLayout.LayoutParams(0, dp(55), 1f)
            )
        }

        root.addView(profiles)

        root.addView(section("EQUALIZER"))

        controls = LinearLayout(this)
        controls.orientation = LinearLayout.VERTICAL
        root.addView(controls)

        root.addView(section("BASS CONTROL"))

        bassSeek = SeekBar(this)
        bassSeek.max = 100
        bassSeek.isEnabled = false

        bassSeek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar?,
                    value: Int,
                    fromUser: Boolean
                ) {
                    try {
                        bassBoost?.setStrength((value * 10).toShort())
                    } catch (_: Exception) {}
                }

                override fun onStartTrackingTouch(seek: SeekBar?) {}
                override fun onStopTrackingTouch(seek: SeekBar?) {}
            }
        )

        root.addView(bassSeek)

        val retry = Button(this)
        retry.text = "RETRY AUDIO CONNECTION"
        retry.setOnClickListener {
            releaseEffects()
            connectAudio()
        }
        root.addView(retry)

        root.addView(section("TV SOUND GUARD • VERSION 1.0"))
    }

    private fun section(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.rgb(105, 190, 255))
            setPadding(0, dp(20), 0, dp(10))
        }
    }

    private fun connectAudio() {
        try {
            equalizer = Equalizer(0, 0)
            equalizer?.enabled = true

            try {
                bassBoost = BassBoost(0, 0)
                bassBoost?.enabled = true
                bassSeek.isEnabled = true
            } catch (_: Exception) {
                bassBoost = null
                bassSeek.isEnabled = false
            }

            status.text =
                "Audio effects connected. Test whether TV firmware permits control."

            status.setTextColor(Color.GREEN)
            createEqualizer()

        } catch (_: Exception) {
            status.text =
                "Global audio control blocked or unsupported by this TV."

            status.setTextColor(Color.RED)

            controls.removeAllViews()

            controls.addView(TextView(this).apply {
                text = "TV firmware has not provided global equalizer access."
                textSize = 15f
                setTextColor(Color.WHITE)
            })
        }
    }

    private fun createEqualizer() {
        controls.removeAllViews()

        val eq = equalizer ?: return

        try {
            val range = eq.bandLevelRange
            val min = range[0].toInt()
            val max = range[1].toInt()

            for (i in 0 until eq.numberOfBands) {
                val band = i.toShort()
                val frequency = eq.getCenterFreq(band) / 1000

                val label = TextView(this).apply {
                    text = "$frequency Hz"
                    textSize = 14f
                    setTextColor(Color.WHITE)
                }

                val seek = SeekBar(this)
                seek.max = max - min
                seek.progress =
                    (eq.getBandLevel(band).toInt() - min)
                        .coerceIn(0, max - min)

                seek.setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            bar: SeekBar?,
                            value: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                try {
                                    eq.setBandLevel(
                                        band,
                                        (value + min).toShort()
                                    )
                                } catch (_: Exception) {}
                            }
                        }

                        override fun onStartTrackingTouch(bar: SeekBar?) {}
                        override fun onStopTrackingTouch(bar: SeekBar?) {}
                    }
                )

                controls.addView(label)
                controls.addView(seek)
            }
        } catch (_: Exception) {
            status.text = "Equalizer bands unavailable."
        }
    }

    private fun applyProfile(profile: String) {
        val eq = equalizer

        if (eq == null) {
            Toast.makeText(
                this,
                "Audio effects unavailable",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val values = when (profile) {
                "Speech" -> listOf(-2, 3, 5, 4, 1)
                "Movie" -> listOf(3, 1, -1, 2, 4)
                "Night" -> listOf(2, 2, 1, 0, -1)
                else -> listOf(0, 0, 0, 0, 0)
            }

            val range = eq.bandLevelRange
            val min = range[0].toInt()
            val max = range[1].toInt()
            val count = eq.numberOfBands.toInt()

            for (i in 0 until count) {
                val value = values[i * values.size / count]
                val level = (value * 100).coerceIn(min, max)

                eq.setBandLevel(i.toShort(), level.toShort())
            }

            if (profile == "Night") {
                try {
                    bassBoost?.setStrength(0)
                    bassSeek.progress = 0
                } catch (_: Exception) {}
            }

            createEqualizer()

            Toast.makeText(
                this,
                "$profile profile applied",
                Toast.LENGTH_SHORT
            ).show()

        } catch (_: Exception) {
            Toast.makeText(
                this,
                "TV rejected audio settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun releaseEffects() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}

        equalizer = null
        bassBoost = null
    }

    override fun onDestroy() {
        releaseEffects()
        super.onDestroy()
    }
}
