package fr.luteal.core.designsystem.component

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import fr.luteal.core.designsystem.theme.LutealTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PainScaleTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tapSelectsAndSecondTapClears() {
        val selected = mutableStateOf<Int?>(null)
        composeRule.setContent {
            LutealTheme {
                PainScale(value = selected.value, onValueChange = { selected.value = it })
            }
        }

        composeRule.onNodeWithTag("pain_scale_0").assert(hasStateDescription("0, Aucune douleur"))
        composeRule.onNodeWithTag("pain_scale_7").performClick()
        composeRule.runOnIdle { assertEquals(7, selected.value) }
        composeRule.onNodeWithTag("pain_scale_7").assert(hasStateDescription("7"))
        composeRule.onNodeWithTag("pain_scale_7").performClick()
        composeRule.runOnIdle { assertNull(selected.value) }
    }

    private fun hasStateDescription(expected: String) = SemanticsMatcher("stateDescription is $expected") { node ->
        node.config.getOrElse(SemanticsProperties.StateDescription) { "" } == expected
    }
}
