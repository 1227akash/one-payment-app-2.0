package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.PaymentStatus
import com.example.ui.components.StatusChip
import com.example.ui.theme.OneTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun statusChip_screenshot() {
        composeTestRule.setContent {
            OneTheme {
                StatusChip(status = PaymentStatus.SUCCESS)
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/status_chip.png")
    }
}
