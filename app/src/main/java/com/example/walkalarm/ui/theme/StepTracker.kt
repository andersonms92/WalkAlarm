package com.example.walkalarm.ui.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepTracker(context: Context, private val targetSteps: Int) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialStepCount: Float? = null
    var currentStepsTaken = 0
        private set

    var onGoalReached: (() -> Unit)? = null
    var onStepUpdated: ((Int) -> Unit)? = null

    fun startListening() {
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0]

            // Define o ponto zero ao disparar o alarme
            if (initialStepCount == null) {
                initialStepCount = totalStepsSinceBoot
            }

            // Calcula quantos passos foram dados APÓS o alarme tocar
            currentStepsTaken = (totalStepsSinceBoot - (initialStepCount ?: totalStepsSinceBoot)).toInt()

            onStepUpdated?.invoke(currentStepsTaken)

            // Verifica se bateu a meta
            if (currentStepsTaken >= targetSteps) {
                onGoalReached?.invoke()
                stopListening()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não necessário para este caso de uso
    }
}