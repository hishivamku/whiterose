/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.linuxh2o.whiterose.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.wear.compose.material3.AppScaffold
import com.linuxh2o.whiterose.presentation.presentation.MainScreen
import com.linuxh2o.whiterose.presentation.presentation.MainViewModel
import com.linuxh2o.whiterose.presentation.theme.WhiteroseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.chimeState.collectAsState()

            WhiteroseTheme {
                AppScaffold {
                    MainScreen(
                        state = state,
                        intervalOptions = viewModel.intervalOptions,
                        onIntervalSelected = { interval ->
                            //viewModel::selectInterval
                            viewModel.selectInterval(interval)
                        },
                        onToggle = {
                            if (state.isActive){
                                viewModel.stop()
                            } else {
                                viewModel.start(state.intervalMinutes)
                            }
                        }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            /*if (!isGranted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }*/
        }
    }
}

