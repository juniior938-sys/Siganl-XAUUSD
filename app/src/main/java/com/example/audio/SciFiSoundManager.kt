package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.SoundAlertTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SciFiSoundManager(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.Default)
  private var isMuted = false
  private var masterVolume = 0.85f
  private var currentTone = SoundAlertTone.MT5_ORDER_CLICK

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun setMuted(muted: Boolean) {
    this.isMuted = muted
  }

  fun isMuted(): Boolean = isMuted

  fun setMasterVolume(vol: Float) {
    this.masterVolume = vol.coerceIn(0f, 1f)
  }

  fun getMasterVolume(): Float = masterVolume

  fun setSelectedTone(tone: SoundAlertTone) {
    this.currentTone = tone
  }

  fun getSelectedTone(): SoundAlertTone = currentTone

  fun playSignalAlert(tone: SoundAlertTone = currentTone, triggerHaptic: Boolean = true) {
    if (isMuted || masterVolume <= 0.01f) {
      if (triggerHaptic) triggerVibration()
      return
    }

    if (triggerHaptic) triggerVibration()

    scope.launch {
      try {
        when (tone) {
          SoundAlertTone.MT5_ORDER_CLICK -> playMt5Click(masterVolume)
          SoundAlertTone.MT5_BELL_ALERT -> playMt5Bell(masterVolume)
          SoundAlertTone.PRICE_PING -> playPricePing(masterVolume)
          SoundAlertTone.PROFIT_CHIME -> playProfitChime(masterVolume)
        }
      } catch (_: Exception) {
        // Fallback gracefully
      }
    }
  }

  private fun triggerVibration() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(
          VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35)
      }
    } catch (_: Exception) {}
  }

  private fun playAudioBuffer(samples: ShortArray) {
    val sampleRate = 44100
    val audioTrack = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build()
      )
      .setBufferSizeInBytes(samples.size * 2)
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()

    audioTrack.write(samples, 0, samples.size)
    audioTrack.play()
    Thread.sleep((samples.size.toDouble() / sampleRate * 1000).toLong() + 30)
    audioTrack.release()
  }

  // 1. Classic MT5 Order Execution Double Click
  private fun playMt5Click(volume: Float) {
    val sampleRate = 44100
    val durationMs = 80
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val samples = ShortArray(numSamples)

    for (i in 0 until numSamples) {
      val tSec = i.toDouble() / sampleRate
      // Double click impulses
      val click1 = if (i in 0..120) sin(2.0 * PI * 1800.0 * tSec) * exp(-i / 30.0) else 0.0
      val click2 = if (i in 400..520) sin(2.0 * PI * 2400.0 * tSec) * exp(-(i - 400) / 30.0) else 0.0
      val raw = (click1 + click2) * volume * 0.95
      samples[i] = (raw * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    playAudioBuffer(samples)
  }

  // 2. MT5 Bell Alert (Clear resonant chime bell)
  private fun playMt5Bell(volume: Float) {
    val sampleRate = 44100
    val durationMs = 350
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val samples = ShortArray(numSamples)

    val freq = 1046.50 // C6
    for (i in 0 until numSamples) {
      val tSec = i.toDouble() / sampleRate
      val progress = i.toDouble() / numSamples
      val env = exp(-6.0 * progress)
      val raw = (sin(2.0 * PI * freq * tSec) + 0.35 * sin(2.0 * PI * freq * 2.0 * tSec)) * env * volume * 0.85
      samples[i] = (raw * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    playAudioBuffer(samples)
  }

  // 3. Price Ping
  private fun playPricePing(volume: Float) {
    val sampleRate = 44100
    val durationMs = 220
    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
    val samples = ShortArray(numSamples)

    val freq = 1318.51 // E6
    for (i in 0 until numSamples) {
      val tSec = i.toDouble() / sampleRate
      val progress = i.toDouble() / numSamples
      val env = exp(-8.5 * progress)
      val raw = sin(2.0 * PI * freq * tSec) * env * volume * 0.9
      samples[i] = (raw * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    playAudioBuffer(samples)
  }

  // 4. Take Profit Soft Chime
  private fun playProfitChime(volume: Float) {
    val sampleRate = 44100
    val notes = doubleArrayOf(783.99, 1046.50) // G5 -> C6
    val noteMs = 120
    val numSamples = (sampleRate * (noteMs * notes.size / 1000.0)).toInt()
    val samples = ShortArray(numSamples)
    val samplesPerNote = (sampleRate * (noteMs / 1000.0)).toInt()

    for (n in notes.indices) {
      val freq = notes[n]
      for (i in 0 until samplesPerNote) {
        val globalIdx = n * samplesPerNote + i
        if (globalIdx < samples.size) {
          val tSec = i.toDouble() / sampleRate
          val progress = i.toDouble() / samplesPerNote
          val env = exp(-4.0 * progress)
          val raw = sin(2.0 * PI * freq * tSec) * env * volume * 0.8
          samples[globalIdx] = (raw * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
      }
    }
    playAudioBuffer(samples)
  }
}
