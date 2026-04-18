package com.linuxh2o.whiterose.presentation.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.linuxh2o.whiterose.presentation.data.ChimeState
import com.linuxh2o.whiterose.presentation.receiver.ChimeReceiver


class ChimeScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, ChimeReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(intervalInMin: Int): ChimeState {
        val nextChimeAt = System.currentTimeMillis() + intervalInMin.toMillis()

        // TODO: setExact()
        setExact(nextChimeAt)

        return ChimeState(
            intervalMinutes = intervalInMin,
            isActive = true,
            nextChimeAt = nextChimeAt
        )
    }

    fun scheduleNext(previousChimeAt: Long, intervalInMin: Int): ChimeState {
        val nextChimeAt = previousChimeAt + intervalInMin.toMillis()

        setExact(nextChimeAt)

        return ChimeState(
            intervalMinutes = intervalInMin,
            isActive = true,
            nextChimeAt = nextChimeAt
        )
    }

    fun rescheduleAfterBoot(state: ChimeState): ChimeState {
        val now = System.currentTimeMillis()

        val nextChimeAt = if (state.nextChimeAt > now) {
            state.nextChimeAt
        } else {
            now + state.intervalMinutes.toMillis()
        }

        setExact(nextChimeAt)

        return state.copy(nextChimeAt = nextChimeAt)
    }

    fun cancel(intervalInMin: Int): ChimeState {
        // no-op, safe to call multiple times
        alarmManager.cancel(buildPendingIntent())

        return ChimeState(
            intervalMinutes = intervalInMin,
            isActive = false,
            nextChimeAt = 0L
        )
    }

    private fun setExact(triggerAtMs: Long) {
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                buildPendingIntent()
            )
        } catch (securityException: SecurityException) {
            Log.e(TAG, "setExact: error", securityException)
        }
    }

    // Alternative: intervalInMin.minutes.inWholeMilliseconds
    // Min to Sec to Milliseconds
    private fun Int.toMillis(): Long = this * 60 * 1000L

    companion object {
        private const val TAG = "ChimeScheduler"
    }
}