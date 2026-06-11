package com.example.services

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Vibrator

object ClickSoundPlayer {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playMechanicalClick(context: Context) {
        try {
            // Short high-pitched key click tone
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            
            // Short crisp vibration
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playLevelUpSound() {
        try {
            // Retro arcade leveling chime
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_7, 200)
            Thread {
                Thread.sleep(150)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_2, 250)
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
