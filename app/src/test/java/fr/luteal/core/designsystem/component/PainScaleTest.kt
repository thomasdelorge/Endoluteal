package fr.luteal.core.designsystem.component

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import fr.luteal.core.designsystem.theme.LutealTheme
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PainScaleTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sliderSetsPainLevel() {
        val selected = mutableStateOf<Int?>(null)
        composeRule.setContent {
            LutealTheme {
                PainScale(value = selected.value, onValueChange = { selected.value = it })
            }
        }

        composeRule.onNodeWithTag("pain_scale_slider").performTouchInput {
            val target = Offset(right * 0.7f, centerY)
            down(target)
            up()
        }
        composeRule.runOnIdle { assertNotNull(selected.value) }
    }
}
