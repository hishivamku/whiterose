package com.linuxh2o.whiterose.presentation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewLargeRound
import com.linuxh2o.whiterose.presentation.data.ChimeState

@WearPreviewLargeRound
@Composable
private fun Preview() {
    //MainScreen()
}

@Composable
fun MainScreen(
    state: ChimeState,
    intervalOptions: List<Int>,
    onIntervalSelected: (Int) -> Unit,
    onToggle: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScreenScaffold(
        scrollState = listState
    ){
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Text(
                    text = "WhiteRose",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    text = if (state.isActive) "Every ${state.intervalMinutes} min" else "Off",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Interval list, two intervals in a row
            items(intervalOptions.chunked(2)) { pair ->
                IntervalRow(
                    pair = pair,
                    selectedInterval = state.intervalMinutes,
                    onIntervalSelected = onIntervalSelected
                )
            }

            item {
                //Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(.7f),
                    colors = if (state.isActive)
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor   = MaterialTheme.colorScheme.onError
                        )
                    else
                        ButtonDefaults.buttonColors()
                ) {
                    Text(
                        text = if (state.isActive) "Stop" else "Start",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun IntervalRow(
    pair: List<Int>,
    selectedInterval: Int,
    onIntervalSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pair.forEach { minutes ->
            val isSelected = minutes == selectedInterval

            Button(
                onClick = { onIntervalSelected(minutes) },
                modifier = Modifier.size(width = 72.dp, height = 36.dp),
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCFB079)
                    )
                } else {
                    ButtonDefaults.filledTonalButtonColors()
                }
            ) {
                Text(
                    text = "$minutes min",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
