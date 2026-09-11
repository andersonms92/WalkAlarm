package com.example.walkalarm.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.walkalarm.ui.theme.StepTracker
import com.example.walkalarm.ui.theme.WalkAlarmTheme

@Composable
fun AlarmScreen(targetSteps: Int = 50, onDismissAlarm: () -> Unit) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var stepsTaken by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        if (isPreview) {
            return@DisposableEffect onDispose {}
        }

        val tracker = StepTracker(context, targetSteps).apply {
            onStepUpdated = { steps ->
                stepsTaken = steps
            }
            onGoalReached = {
                onDismissAlarm()
            }
            startListening()
        }

        onDispose {
            tracker.stopListening()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "ACORDE! CAMINHE PARA DESLIGAR", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "$stepsTaken / $targetSteps",
            style = MaterialTheme.typography.displayLarge
        )
        Text(text = "passos dados", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    WalkAlarmTheme {
        AlarmScreen(targetSteps = 50, onDismissAlarm = {})
    }
}
