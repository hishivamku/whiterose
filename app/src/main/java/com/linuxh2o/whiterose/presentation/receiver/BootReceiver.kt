package com.linuxh2o.whiterose.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.linuxh2o.whiterose.presentation.data.ChimePref
import com.linuxh2o.whiterose.presentation.scheduler.ChimeScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val prefs = ChimePref(context)
                val state = prefs.read()

                if (!state.isActive) return@launch

                val scheduler = ChimeScheduler(context)
                val updatedState = scheduler.rescheduleAfterBoot(state)

                // Persist state for the next scheduled chime
                prefs.save(updatedState)
            } finally {
                // Release it or it will result in ANR in 10 seconds.
                pendingResult.finish()
            }
        }

    }
}