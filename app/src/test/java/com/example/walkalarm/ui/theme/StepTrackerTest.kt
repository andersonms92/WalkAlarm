package com.example.walkalarm.ui.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.lang.reflect.Field

class StepTrackerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var sensorManager: SensorManager

    @Mock
    private lateinit var stepCounterSensor: Sensor

    private lateinit var stepTracker: StepTracker

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        `when`(context.getSystemService(Context.SENSOR_SERVICE)).thenReturn(sensorManager)
        `when`(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)).thenReturn(stepCounterSensor)
        
        stepTracker = StepTracker(context, 10)
    }

    @Test
    fun `test step increment logic`() {
        var stepsUpdated = 0
        stepTracker.onStepUpdated = { stepsUpdated = it }

        val event1 = createMockSensorEvent(100f)
        stepTracker.onSensorChanged(event1)
        assertEquals(0, stepsUpdated)

        val event2 = createMockSensorEvent(105f)
        stepTracker.onSensorChanged(event2)
        assertEquals(5, stepsUpdated)
        assertEquals(5, stepTracker.currentStepsTaken)
    }

    @Test
    fun `test goal reached logic`() {
        var goalReached = false
        stepTracker.onGoalReached = { goalReached = true }

        stepTracker.onSensorChanged(createMockSensorEvent(100f))

        stepTracker.onSensorChanged(createMockSensorEvent(110f))
        
        assertEquals(true, goalReached)
    }

    private fun createMockSensorEvent(value: Float): SensorEvent {
        val event = mock(SensorEvent::class.java)

        val valuesField: Field = SensorEvent::class.java.getField("values")
        valuesField.isAccessible = true
        valuesField.set(event, floatArrayOf(value))

        val sensorField: Field = SensorEvent::class.java.getField("sensor")
        sensorField.isAccessible = true
        val mockSensor = mock(Sensor::class.java)
        `when`(mockSensor.type).thenReturn(Sensor.TYPE_STEP_COUNTER)
        sensorField.set(event, mockSensor)

        return event
    }
}
