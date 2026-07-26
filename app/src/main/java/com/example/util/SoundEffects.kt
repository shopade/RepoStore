package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

class SoundEffects {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Could not initialize ToneGenerator", e)
        }
    }

    fun playRollClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing roll click", e)
        }
    }

    fun playLandingSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 60)
        } catch (e: Exception) {
            Log.e("SoundEffects", "Error playing landing sound", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
