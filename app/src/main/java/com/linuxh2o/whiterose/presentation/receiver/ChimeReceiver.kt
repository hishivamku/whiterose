package com.linuxh2o.whiterose.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.linuxh2o.whiterose.R
import com.linuxh2o.whiterose.presentation.data.ChimePref
import com.linuxh2o.whiterose.presentation.scheduler.ChimeScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChimeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope.launch {
            try {
                val prefs = ChimePref(context)
                val state = prefs.read()

                // Guard: If user stopped chiming while this alarm was in flight, skip.
                if (!state.isActive) return@launch

                // Fire chime and vibration
                // waits for ALL children before continuing
                coroutineScope {
                    launch { vibrate(context) }
                    launch { playChime(context) }
                }

                // Schedule next
                val scheduler = ChimeScheduler(context)
                val nextState = scheduler.scheduleNext(
                    previousChimeAt = state.nextChimeAt,
                    intervalInMin = state.intervalMinutes
                )

                // Persist state for the next scheduled chime
                prefs.save(nextState)
            } finally {
                // Release it or it will result in ANR in 10 seconds.
                pendingResult.finish()
            }
        }
    }

    private suspend fun vibrate(context: Context) {
        delay(100L)

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect = VibrationEffect.createWaveform(
            longArrayOf(0, 250, 200, 250, 200, 250, 200),
            intArrayOf( 0, 255,   0, 255,   0, 255,   0),
            -1
        )

        // Vibration not working after 1st call fix.
        // Attributes tells the OS this is alarm class vibration.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes = android.os.VibrationAttributes.Builder()
                .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                .build()
            vibrator.vibrate(effect, attributes)
        } else {
            vibrator.vibrate(effect)
        }

        Log.d("ChimeReceiver", "vibrate() returned")
    }

    private suspend fun playChime(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(context.resources.openRawResourceFd(R.raw.chime))
            prepare()
        }

        mediaPlayer.setOnCompletionListener {
            it.reset()
            it.release()
            Log.d("ChimeReceiver", "playChime: completed")
        }

        mediaPlayer.start()

        val durationMs = mediaPlayer.duration.toLong()
        delay(durationMs + 500L)
    }
}