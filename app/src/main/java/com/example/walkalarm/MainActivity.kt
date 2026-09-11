package com.example.walkalarm

import android.Manifest
import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.walkalarm.service.AlarmService
import com.example.walkalarm.ui.screen.AlarmScreen
import com.example.walkalarm.ui.theme.WalkAlarmTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private var targetStepsState by mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CheckPermissions()
            WalkAlarmTheme {
                if (targetStepsState != -1) {
                    AlarmScreen(
                        targetSteps = targetStepsState,
                        onDismissAlarm = {
                            stopAlarmService()
                            targetStepsState = -1
                        }
                    )
                } else {
                    ConfigurationScreen(
                        onScheduleAlarm = { hour, minute, steps ->
                            scheduleAlarm(hour, minute, steps)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val target = intent.getIntExtra(AlarmReceiver.EXTRA_TARGET_STEPS, -1)
        if (target != -1) {
            targetStepsState = target
            turnScreenOnAndUnlock()
        }
    }

    private fun scheduleAlarm(hour: Int, minute: Int, steps: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TARGET_STEPS, steps)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(this,
            getString(R.string.alarme_definido_para,
                hour,
                String.format("%02d", minute)), Toast.LENGTH_SHORT).show()
    }

    private fun turnScreenOnAndUnlock() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    private fun stopAlarmService() {
        val serviceIntent = Intent(this, AlarmService::class.java)
        stopService(serviceIntent)
    }
}

@Composable
fun ConfigurationScreen(onScheduleAlarm: (Int, Int, Int) -> Unit) {
    val context = LocalContext.current
    var selectedTime by remember { mutableStateOf("Selecionar Hora") }
    var hour by remember { mutableIntStateOf(0) }
    var minute by remember { mutableIntStateOf(0) }
    var steps by remember { mutableStateOf("50") }


    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Configurar Despertador", style = MaterialTheme.typography.headlineLarge)
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            hour = h
                            minute = m
                            selectedTime = "${h}:${String.format("%02d", m)}"
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedTime)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Meta de Passos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onScheduleAlarm(hour, minute, steps.toIntOrNull() ?: 50)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTime != "Selecionar Hora"
            ) {
                Text(text = "Agendar Alarme")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    WalkAlarmTheme {
        ConfigurationScreen(onScheduleAlarm = { _, _, _ -> })
    }
}

@Composable
fun CheckPermissions() {
    val context = LocalContext.current
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("Permission", "${it.key} = ${it.value}")
        }
    }

    LaunchedEffect(Unit) {
        val needsRequest = permissionsToRequest.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needsRequest) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
}
