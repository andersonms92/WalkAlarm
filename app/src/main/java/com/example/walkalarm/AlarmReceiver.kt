package com.example.walkalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.walkalarm.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val targetSteps = intent.getIntExtra(EXTRA_TARGET_STEPS, 50)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(EXTRA_TARGET_STEPS, targetSteps)
        }

        context.startForegroundService(serviceIntent)
    }

    companion object {
        const val EXTRA_TARGET_STEPS = "extra_target_steps"
    }
}