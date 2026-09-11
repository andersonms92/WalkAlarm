package com.example.walkalarm

import android.content.Context
import android.content.Intent
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.mockConstruction
import org.mockito.MockitoAnnotations

class AlarmReceiverTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var intent: Intent

    private lateinit var alarmReceiver: AlarmReceiver

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        alarmReceiver = AlarmReceiver()
    }

    @Test
    fun `test onReceive starts AlarmService with correct extras`() {
        val targetSteps = 100
        Mockito.`when`(intent.getIntExtra(AlarmReceiver.EXTRA_TARGET_STEPS, 50)).thenReturn(targetSteps)

        mockConstruction(Intent::class.java).use { mock ->
            alarmReceiver.onReceive(context, intent)
            
            val capturedIntent = mock.constructed().first()
            verify(context).startForegroundService(capturedIntent)
            verify(capturedIntent).putExtra(AlarmReceiver.EXTRA_TARGET_STEPS, targetSteps)
        }
    }
}
