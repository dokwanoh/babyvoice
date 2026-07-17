package com.babyvoice.bridge.feature.briefing

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.babyvoice.bridge.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BriefingDeepLinkUiAutomatorTest {
    @Test
    fun briefingDeepLink_displays_briefing_content() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("babyvoice://briefing"))
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        ActivityScenario.launch<MainActivity>(intent).use {
            assertTrue(device.wait(Until.hasObject(By.textContains("630밀리리터")), 5_000))
            assertTrue(device.wait(Until.hasObject(By.textContains("마지막 분유는")), 5_000))
        }
    }

    @Test
    fun launcherOpen_displays_briefing_content() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        ActivityScenario.launch<MainActivity>(intent).use {
            assertTrue(device.wait(Until.hasObject(By.textContains("630밀리리터")), 5_000))
            assertTrue(device.wait(Until.hasObject(By.textContains("마지막 분유는")), 5_000))
        }
    }
}
