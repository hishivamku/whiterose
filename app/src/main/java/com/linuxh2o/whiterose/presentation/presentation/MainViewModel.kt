package com.linuxh2o.whiterose.presentation.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.linuxh2o.whiterose.presentation.data.ChimePref
import com.linuxh2o.whiterose.presentation.data.ChimeState
import com.linuxh2o.whiterose.presentation.scheduler.ChimeScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application = application) {
    private val prefs     = ChimePref(application)
    private val scheduler = ChimeScheduler(application)

    val chimeState: StateFlow<ChimeState> = prefs.chimeState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChimeState(
                intervalMinutes = 1,
                isActive = false,
                nextChimeAt = 0L
            )
        )

    val intervalOptions = prefs.intervalOptions

    fun start(intervalMinutes: Int) {
        Log.d("MainViewModel", "start: called")
        viewModelScope.launch {
            val newState = scheduler.schedule(intervalMinutes)
            prefs.save(newState)
        }
    }

    fun stop() {
        Log.d("MainViewModel", "stop: called")
        viewModelScope.launch {
            val currentInterval = chimeState.value.intervalMinutes
            val clearedState = scheduler.cancel(currentInterval)
            prefs.save(clearedState)
        }
    }

    fun selectInterval(intervalMinutes: Int) {
        Log.d("MainViewModel", "selectInterval: called")
        viewModelScope.launch {
            val isActive = chimeState.value.isActive
            if (isActive) {
                val newState = scheduler.schedule(intervalMinutes)
                prefs.save(newState)
            } else {
                prefs.save(chimeState.value.copy(intervalMinutes = intervalMinutes))
            }
        }
    }
}