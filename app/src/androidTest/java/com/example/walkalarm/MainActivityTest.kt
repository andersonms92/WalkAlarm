package com.example.walkalarm

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testConfigurationScreenDisplaysCorrectly() {
        composeTestRule.onNodeWithText("Configurar Despertador").assertIsDisplayed()

        composeTestRule.onNodeWithText("Selecionar Hora").assertIsDisplayed()

        composeTestRule.onNodeWithText("Meta de Passos").assertIsDisplayed()

        composeTestRule.onNodeWithText("Agendar Alarme").assertIsNotEnabled()
    }

    @Test
    fun testStepsInputChangesValue() {
        val inputField = composeTestRule.onNodeWithText("Meta de Passos")

        inputField.performTextClearance()
        inputField.performTextInput("100")
        
        inputField.assertTextContains("100")
    }
}
