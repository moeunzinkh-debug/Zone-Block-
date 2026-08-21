package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GameProfile
import com.example.ui.viewmodel.EdgeProtectionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string resources and accessibility description`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Edge Protection", appName)

        val serviceDesc = context.getString(R.string.accessibility_service_description)
        assertTrue(serviceDesc.isNotEmpty())
    }

    @Test
    fun `viewModel handles edge protection toggling and profiles`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = EdgeProtectionViewModel(application)

        assertNotNull(viewModel.config.value)
        assertEquals(13, viewModel.config.value.zoneSizePercent)

        viewModel.setZoneSizePercent(16)
        assertEquals(16, viewModel.config.value.zoneSizePercent)

        viewModel.selectProfile(GameProfile.FPS_SHOOTER)
        assertEquals(18, viewModel.config.value.zoneSizePercent)
        assertEquals(GameProfile.FPS_SHOOTER.title, viewModel.config.value.profileName)
    }
}

